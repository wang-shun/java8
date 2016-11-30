package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.search.group.GroupSearchWriteService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/30
 */
@Slf4j
@Component
public class DoctorGroupEventListener implements EventListener {

    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Autowired
    private GroupSearchWriteService groupSearchWriteService;

    @Autowired
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    private DoctorDailyReportCache doctorDailyReportCache;

    @Subscribe
    public void handleGroupEvent(ListenedGroupEvent groupEvent) {
        log.info("[DoctorGroupEventListener]-> handle.group.event, groupEvent -> {}", groupEvent);

        //更新猪群搜索
        groupSearchWriteService.update(groupEvent.getGroupId());

        DoctorGroupEvent event = doctorGroupEventDao.findById(groupEvent.getDoctorGroupEventId());
        if (event == null) {
            log.error("handle group event({}), but event not found!", groupEvent);
            return;
        }

        GroupEventType eventType = GroupEventType.from(event.getType());
        if (eventType == null) {
            log.error("handle group event type not find, groupEvent:{}, event:{}", groupEvent, event);
            return;
        }

        switch (eventType) {
            case MOVE_IN:
                handleGroupLiveStock(event);
                break;
            case CHANGE:
                handleGroupLiveStock(event);
                handleChange(event);
                break;
            case TRANS_GROUP:
                handleGroupLiveStock(event);
                break;
            case TURN_SEED:
                handleGroupLiveStock(event);
                break;
            case TRANS_FARM:
                handleGroupLiveStock(event);
                break;
            default:
                break;
        }
    }

    //处理变动事件
    private void handleChange(DoctorGroupEvent event) {
        Date startAt = Dates.startOfDay(event.getEventAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(event.getEventAt())).toDate();

        if (Objects.equals(event.getChangeTypeId(), DoctorBasicEnums.DEAD.getId())
                || Objects.equals(event.getChangeTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            getDead(PigType.from(event.getPigType()), event.getFarmId(), startAt, endAt);
            return;
        }
        if (Objects.equals(event.getChangeTypeId(), DoctorBasicEnums.SALE.getId())) {
            getSale(PigType.from(event.getPigType()), event.getFarmId(), startAt, endAt);
        }
    }

    //更新存栏相关的统计
    private void handleGroupLiveStock(DoctorGroupEvent event) {
        Date startAt = Dates.startOfDay(event.getEventAt());
        Date endAt = Dates.startOfDay(new Date());
        Long farmId = event.getFarmId();

        //更新数据库的存栏统计
        doctorPigTypeStatisticWriteService.statisticGroup(event.getOrgId(), event.getFarmId());

        PigType pigType = PigType.from(event.getPigType());
        if (pigType == null) {
            log.error("group event pigType({}) not support! eventId:{}", event.getPigType(), event.getId());
            return;
        }

        //更新到今天的存栏
        while (!startAt.after(endAt)) {
            //查询startAt 这条的日报是否存在，如果已经初始化过了，则不做处理
            if (!doctorDailyReportCache.reportIsFullInit(event.getFarmId(), startAt)) {
                getLiveStock(pigType, farmId, startAt);
            }
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
    }

    //根据每个类型更新相应的存栏
    private void getLiveStock(PigType pigType, Long farmId, Date startAt) {
        switch (pigType) {
            case NURSERY_PIGLET:
                int nursery = doctorKpiDao.realTimeLiveStockNursery(farmId, startAt);
                DoctorDailyReportDto reportDtoNursery = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoNursery.getLiveStock().setNursery(nursery);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoNursery);
                break;
            case FATTEN_PIG:
                int fatten = doctorKpiDao.realTimeLiveStockFatten(farmId, startAt);
                DoctorDailyReportDto reportDtoFatten = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFatten.getLiveStock().setFatten(fatten);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFatten);
                break;
            case RESERVE:
                int houbeiSow = doctorKpiDao.realTimeLiveStockHoubeiSow(farmId, startAt);
                int houbeiBoar = doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId, startAt);
                DoctorDailyReportDto reportDtoHoubei = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoHoubei.getLiveStock().setHoubeiSow(houbeiSow);
                reportDtoHoubei.getLiveStock().setHoubeiBoar(houbeiBoar);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoHoubei);
                break;
            case DELIVER_SOW:
                int farrow = doctorKpiDao.realTimeLiveStockFarrow(farmId, startAt);
                DoctorDailyReportDto reportDtoFarrow = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFarrow.getLiveStock().setFarrow(farrow);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFarrow);
                break;
            default:
                break;
        }
    }

    //处理死淘
    private void getDead(PigType pigType, Long farmId, Date startAt, Date endAt) {
        switch (pigType) {
            case NURSERY_PIGLET:
                int nursery = doctorKpiDao.getDeadNursery(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoNursery = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoNursery.getDead().setNursery(nursery);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoNursery);
                break;
            case FATTEN_PIG:
                int fatten = doctorKpiDao.getDeadFatten(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoFatten = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFatten.getDead().setFatten(fatten);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFatten);
                break;
            case RESERVE:
                int houbei = doctorKpiDao.getDeadHoubei(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoHoubei = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoHoubei.getDead().setHoubei(houbei);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoHoubei);
                break;
            case DELIVER_SOW:
                int farrow = doctorKpiDao.getDeadFarrow(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoFarrow = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFarrow.getDead().setFarrow(farrow);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFarrow);
                break;
            default:
                break;
        }
    }

    //处理销售
    private void getSale(PigType pigType, Long farmId, Date startAt, Date endAt) {
        log.info("handle group sale type:{}, startAt:{}, endAt:{}", pigType, startAt, endAt);
        if (Objects.equals(pigType, PigType.FATTEN_PIG)) {
            int fatten = doctorKpiDao.getSaleFatten(farmId, startAt, endAt);
            long fattenPrice = doctorKpiDao.getGroupSaleFattenPrice(farmId, startAt, endAt);
            DoctorDailyReportDto reportDtoFatten = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
            reportDtoFatten.getSale().setFatten(fatten);
            reportDtoFatten.getSale().setFattenPrice(fattenPrice);
            doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFatten);
            return;
        }

        int nursery = doctorKpiDao.getSaleNursery(farmId, startAt, endAt);
        long base10 = doctorKpiDao.getGroupSaleBasePrice10(farmId, startAt, endAt);
        long base15 = doctorKpiDao.getGroupSaleBasePrice15(farmId, startAt, endAt);

        DoctorDailyReportDto reportDtoNursery = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
        reportDtoNursery.getSale().setNursery(nursery);
        reportDtoNursery.getSale().setBasePrice10(base10);
        reportDtoNursery.getSale().setBasePrice15(base15);
        doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoNursery);
    }
}
