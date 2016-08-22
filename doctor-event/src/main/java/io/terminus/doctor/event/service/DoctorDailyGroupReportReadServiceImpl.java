package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.CountUtil.intStream;
import static io.terminus.doctor.common.utils.CountUtil.longStream;

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
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorDailyGroupReportReadServiceImpl(DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                                 DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService,
                                                 DoctorGroupReadService doctorGroupReadService,
                                                 DoctorBarnReadService doctorBarnReadService) {
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorBarnReadService = doctorBarnReadService;
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
        liveStockReport.setHoubeiSow(MoreObjects.firstNonNull(getHoubeiSow(event.getFarmId()), 0));
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
        report.setSale(saleReport);

        log.info("daily group report doReportByEvent:{}", report);

        return report;
    }

    //执行统计方法
    private DoctorDailyReportDto doReport(DoctorPigTypeStatistic statistic, Long farmId, Date date) {
        DoctorDailyReportDto report = new DoctorDailyReportDto();
        report.setFarmId(farmId);
        report.setSumAt(Dates.startOfDay(date));

        DoctorLiveStockDailyReport liveStockReport = new DoctorLiveStockDailyReport();

        //存栏
        if (statistic != null) {
            liveStockReport.setFarrow(MoreObjects.firstNonNull(statistic.getFarrow(), 0));
            liveStockReport.setNursery(MoreObjects.firstNonNull(statistic.getNursery(), 0));
            liveStockReport.setFatten(MoreObjects.firstNonNull(statistic.getFatten(), 0));
        }

        //后备母猪存栏
        liveStockReport.setHoubeiSow(getHoubeiSow(farmId));
        report.setLiveStock(liveStockReport);

        //取出转换好的事件(按时间区间和事件类型查询的事件, 为保险起见, 查询区间为一天的开始与结束)
        List<ChangeEvent> events = RespHelper.orServEx(
                doctorGroupReadService.findGroupEventsByEventTypeAndDate(farmId, GroupEventType.CHANGE.getValue(), Dates.startOfDay(date), Dates.endOfDay(date)))
                .stream()
                .map(this::getChangeEvent)
                .collect(Collectors.toList());

        //死淘
        DoctorDeadDailyReport deadReport = new DoctorDeadDailyReport();
        //分娩舍里有产房仔猪
        deadReport.setFarrow(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.FARROW_PIGLET) || isDeadEvent(e, PigType.DELIVER_SOW)), ChangeEvent::getQuantity).sum());
        deadReport.setNursery(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.NURSERY_PIGLET)), ChangeEvent::getQuantity).sum());
        deadReport.setFatten(intStream(filterChangeEvent(events, e -> isDeadEvent(e, PigType.FATTEN_PIG)), ChangeEvent::getQuantity).sum());
        report.setDead(deadReport);

        //销售 (保育 = 产房 + 保育)
        DoctorSaleDailyReport saleReport = new DoctorSaleDailyReport();
        saleReport.setNursery(longStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.FARROW_PIGLET)), c -> c.getChange().getAmount()).sum() / 100
                + (longStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.NURSERY_PIGLET)), c -> c.getChange().getAmount()).sum() / 100)
                + (longStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.DELIVER_SOW)), c -> c.getChange().getAmount()).sum() / 100));
        saleReport.setFatten(longStream(filterChangeEvent(events, e -> isSaleEvent(e, PigType.FATTEN_PIG)), c -> c.getChange().getAmount()).sum() / 100);
        report.setSale(saleReport);

        //猪群每日的存栏 Map: key = groupId, value = quantity
        DoctorGroupSearchDto search = new DoctorGroupSearchDto();
        search.setFarmId(farmId);
        Map<Long, Integer> groupCountMap = RespHelper.orServEx(doctorGroupReadService.findGroupDetail(search)).stream()
                .collect(Collectors.toMap(k -> k.getGroup().getId(), v -> v.getGroupTrack().getQuantity()));
        report.setGroupCountMap(groupCountMap);
        log.info("daily group report doReport:{}", report);

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

    //过滤事件
    private List<ChangeEvent> filterChangeEvent(List<ChangeEvent> events, Predicate<ChangeEvent> predicate) {
        return events.stream().filter(predicate).collect(Collectors.toList());
    }

    //转换成变动事件
    private ChangeEvent getChangeEvent(DoctorGroupEvent event) {
        DoctorChangeGroupEvent changeGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), JSON_MAPPER.createCollectionType(DoctorChangeGroupEvent.class));
        return new ChangeEvent(event.getPigType(), event.getQuantity(), changeGroupEvent);
    }

    //获取后备母猪存栏
    private Integer getHoubeiSow(Long farmId) {
        List<DoctorBarn> houbeiBarns = RespHelper.orServEx(doctorBarnReadService
                .findBarnsByFarmIdAndPigTypes(farmId, Lists.newArrayList(PigType.RESERVE_SOW.getValue())));
        Integer houbei = 0;
        for (DoctorBarn houbeiBarn : houbeiBarns) {
            houbei += RespHelper.or(doctorBarnReadService.countPigByBarnId(houbeiBarn.getId()), 0);
        }
        return houbei;
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
