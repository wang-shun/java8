package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@Service
public class DoctorDailyGroupReportReadServiceImpl implements DoctorDailyGroupReportReadService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyGroupReportReadServiceImpl(DoctorGroupReadService doctorGroupReadService,
                                                 DoctorKpiDao doctorKpiDao) {
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public Response<DoctorDailyReportDto> getGroupDailyReportByEventId(Long eventId) {
        try {
            DoctorGroupEvent event = RespHelper.orServEx(doctorGroupReadService.findGroupEventById(eventId));
            return Response.ok(doReportByEvent(event));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get group daily report by eventId failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.daily.report.fail");
        }
    }

    //根据事件获取report
    private DoctorDailyReportDto doReportByEvent(DoctorGroupEvent event) {
        Date startAt = Dates.startOfDay(event.getEventAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();

        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setFarmId(event.getFarmId());
        report.setSumAt(startAt);

        //存栏每次一定要更新
        DoctorLiveStockDailyReport liveStockReport = new DoctorLiveStockDailyReport();
        liveStockReport.setFarrow(doctorKpiDao.realTimeLiveStockFarrow(report.getFarmId(), startAt));
        liveStockReport.setNursery(doctorKpiDao.realTimeLiveStockNursery(report.getFarmId(), startAt));
        liveStockReport.setFatten(doctorKpiDao.realTimeLiveStockFatten(report.getFarmId(), startAt));
        liveStockReport.setHoubeiSow(doctorKpiDao.realTimeLiveStockHoubeiSow(report.getFarmId(), startAt));
        liveStockReport.setHoubeiBoar(doctorKpiDao.realTimeLiveStockHoubeiBoar(report.getFarmId(), startAt));
        report.setLiveStock(liveStockReport);

        //不是变动事件, 直接返回
        if (!Objects.equals(event.getType(), GroupEventType.CHANGE.getValue())) {
            return report;
        }

        ChangeEvent changeEvent = getChangeEvent(event);
        //死淘
        DoctorDeadDailyReport deadReport = new DoctorDeadDailyReport();
        deadReport.setFarrow((isDeadEvent(changeEvent, PigType.FARROW_PIGLET) ? changeEvent.getQuantity() : 0) +
                (isDeadEvent(changeEvent, PigType.DELIVER_SOW) ? changeEvent.getQuantity() : 0));
        deadReport.setNursery(isDeadEvent(changeEvent, PigType.NURSERY_PIGLET) ? changeEvent.getQuantity() : 0);
        deadReport.setFatten(isDeadEvent(changeEvent, PigType.FATTEN_PIG) ? changeEvent.getQuantity() : 0);
        report.setDead(deadReport);

        //销售 (保育 = 产房 + 保育)
        DoctorSaleDailyReport saleReport = new DoctorSaleDailyReport();
        saleReport.setNursery(isSaleEvent(changeEvent, PigType.FARROW_PIGLET) || isSaleEvent(changeEvent, PigType.NURSERY_PIGLET)
                || isSaleEvent(changeEvent, PigType.DELIVER_SOW) ? changeEvent.getChange().getAmount() / 100 : 0);
        saleReport.setFatten(isSaleEvent(changeEvent, PigType.FATTEN_PIG) ? changeEvent.getChange().getAmount() / 100 : 0);

        //销售均价
        saleReport.setBasePrice10(doctorKpiDao.getGroupSaleBasePrice10(report.getFarmId(), startAt, endAt));
        saleReport.setBasePrice15(doctorKpiDao.getGroupSaleBasePrice15(report.getFarmId(), startAt, endAt));
        saleReport.setFattenPrice(doctorKpiDao.getGroupSaleFattenPrice(report.getFarmId(), startAt, endAt));

        report.setSale(saleReport);

        log.info("daily group report doReportByEvent:{}", report);

        return report;
    }

    //区别出猪类的死淘
    private boolean isDeadEvent(ChangeEvent e, PigType pigType) {
        return Objects.equals(e.getPigType(), pigType.getValue())
                && (Objects.equals(DoctorBasicEnums.DEAD.getId(), e.getChange().getChangeTypeId())
                || Objects.equals(DoctorBasicEnums.ELIMINATE.getId(), e.getChange().getChangeTypeId()));
    }

    //区别出猪类的销售
    private boolean isSaleEvent(ChangeEvent e, PigType pigType) {
        return Objects.equals(e.getPigType(), pigType.getValue())
                && Objects.equals(DoctorBasicEnums.SALE.getId(), e.getChange().getChangeTypeId());
    }

    //转换成变动事件
    private ChangeEvent getChangeEvent(DoctorGroupEvent event) {
        DoctorChangeGroupEvent changeGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), JSON_MAPPER.createCollectionType(DoctorChangeGroupEvent.class));
        return new ChangeEvent(event.getPigType(), event.getQuantity(), changeGroupEvent);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class ChangeEvent {

        /**
         * 猪群类型
         */
        private Integer pigType;

        /**
         * 事件猪只数
         */
        private Integer quantity;

        /**
         * 变动细节
         */
        private DoctorChangeGroupEvent change;
    }
}
