package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.dto.report.DoctorSaleDailyReport;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.CountUtil.intStream;

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

    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorDailyGroupReportReadServiceImpl(DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                                 DoctorGroupReadService doctorGroupReadService) {
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public Response<DoctorDailyReportDto> getGroupDailyReportByFarmIdAndDate(Long farmId, Date date) {
        try {
            DoctorPigTypeStatistic statistic = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farmId));
            return Response.ok(doReport(statistic, farmId, date));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get group daily report by farmId and date failed, farmId:{}, date:{}, cause:{}",
                    farmId, date, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.daily.report.fail");
        }
    }

    @Override
    public Response<List<DoctorDailyReportDto>> getGroupDailyReportsByDate(Date date) {
        try {
            List<DoctorDailyReportDto> reports = RespHelper.orServEx(doctorPigTypeStatisticReadService.finaAllPigTypeStatistics()).stream()
                    .map(stat -> doReport(stat, stat.getFarmId(), date))
                    .collect(Collectors.toList());

            return Response.ok(reports);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get group daily report by farmId and date failed, date:{}, cause:{}",
                    date, Throwables.getStackTraceAsString(e));
            return Response.fail("get.group.daily.report.fail");
        }
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
        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setFarmId(event.getFarmId());
        report.setSumAt(Dates.startOfDay(event.getEventAt()));

        //存栏每次一定要更新
        DoctorPigTypeStatistic statistic = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(event.getFarmId()));
        DoctorLiveStockDailyReport liveStockReport = new DoctorLiveStockDailyReport();
        liveStockReport.setFarrow(MoreObjects.firstNonNull(statistic.getFarrow(), 0));
        liveStockReport.setNursery(MoreObjects.firstNonNull(statistic.getNursery(), 0));
        liveStockReport.setFatten(MoreObjects.firstNonNull(statistic.getFatten(), 0));
        report.setLiveStock(liveStockReport);

        //不是变动事件, 直接返回
        if (!Objects.equals(event.getType(), GroupEventType.CHANGE.getValue())) {
            return report;
        }

        ChangeEvent changeEvent = getChangeEvent(event);
        //死淘
        DoctorDeadDailyReport deadReport = new DoctorDeadDailyReport();
        deadReport.setFarrow(isDeadEvent(changeEvent, PigType.FARROW_PIGLET) ? changeEvent.getQuantity() : 0);
        deadReport.setNursery(isDeadEvent(changeEvent, PigType.NURSERY_PIGLET) ? changeEvent.getQuantity() : 0);
        deadReport.setFatten(isDeadEvent(changeEvent, PigType.FATTEN_PIG) ? changeEvent.getQuantity() : 0);
        report.setDead(deadReport);

        //销售 (保育 = 产房 + 保育)
        DoctorSaleDailyReport saleReport = new DoctorSaleDailyReport();
        saleReport.setNursery(isSaleEvent(changeEvent, PigType.FARROW_PIGLET)
                || isSaleEvent(changeEvent, PigType.NURSERY_PIGLET) ? changeEvent.getQuantity() : 0);
        saleReport.setFatten(isSaleEvent(changeEvent, PigType.FATTEN_PIG) ? changeEvent.getQuantity() : 0);
        report.setSale(saleReport);

        log.info("daily group report doReportByEvent:{}", report);

        return report;
    }

    //执行统计方法
    private DoctorDailyReportDto doReport(DoctorPigTypeStatistic statistic, Long farmId, Date date) {
        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setFarmId(farmId);
        report.setSumAt(Dates.startOfDay(date));

        //存栏
        if (statistic != null) {
            DoctorLiveStockDailyReport liveStockReport = new DoctorLiveStockDailyReport();
            liveStockReport.setFarrow(MoreObjects.firstNonNull(statistic.getFarrow(), 0));
            liveStockReport.setNursery(MoreObjects.firstNonNull(statistic.getNursery(), 0));
            liveStockReport.setFatten(MoreObjects.firstNonNull(statistic.getFatten(), 0));
            report.setLiveStock(liveStockReport);
        }

        //取出转换好的事件(按时间区间和事件类型查询的事件)
        List<ChangeEvent> events = RespHelper.orServEx(
                doctorGroupReadService.findGroupEventsByEventTypeAndDate(farmId, GroupEventType.CHANGE.getValue(), Dates.startOfDay(date), date))
                .stream()
                .map(this::getChangeEvent)
                .collect(Collectors.toList());

        //死淘
        DoctorDeadDailyReport deadReport = new DoctorDeadDailyReport();
        deadReport.setFarrow(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.FARROW_PIGLET)), ChangeEvent::getQuantity).sum());
        deadReport.setNursery(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.NURSERY_PIGLET)), ChangeEvent::getQuantity).sum());
        deadReport.setFatten(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.FATTEN_PIG)), ChangeEvent::getQuantity).sum());
        report.setDead(deadReport);

        //销售 (保育 = 产房 + 保育)
        DoctorSaleDailyReport saleReport = new DoctorSaleDailyReport();
        saleReport.setNursery(intStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.FARROW_PIGLET)), ChangeEvent::getQuantity).sum()
                + intStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.NURSERY_PIGLET)), ChangeEvent::getQuantity).sum());
        saleReport.setFatten(intStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.FATTEN_PIG)), ChangeEvent::getQuantity).sum());
        report.setSale(saleReport);

        log.info("daily group report doReport:{}", report);

        return report;
    }

    //区别出猪类的死淘
    private boolean isDeadEvent(ChangeEvent e, PigType pigType) {
        return Objects.equals(e.getPigType(), pigType.getValue())
                && (Objects.equals(DoctorBasicEnums.DEAD.getId(), e.getChange().getChangeReasonId())
                || Objects.equals(DoctorBasicEnums.ELIMINATE.getId(), e.getChange().getChangeReasonId()));
    }

    //区别出猪类的销售
    private boolean isSaleEvent(ChangeEvent e, PigType pigType) {
        return Objects.equals(e.getPigType(), pigType.getValue())
                && Objects.equals(DoctorBasicEnums.SALE.getId(), e.getChange().getChangeReasonId());
    }

    //过滤事件
    private List<ChangeEvent> filterChangeEvent(List<ChangeEvent> events, Predicate<ChangeEvent> predicate) {
        return events.stream().filter(predicate).collect(Collectors.toList());
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
