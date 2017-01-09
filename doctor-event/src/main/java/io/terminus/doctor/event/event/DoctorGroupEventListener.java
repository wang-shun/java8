package io.terminus.doctor.event.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.GroupEventType;
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
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    private DoctorDailyReportCache doctorDailyReportCache;

    @AllowConcurrentEvents
    @Subscribe
    public void handleGroupEvent(ListenedGroupEvent groupEvent) {
        // TODO: 2017/1/9
    }

    private void handle(Long orgId, Long farmId, Integer pigType, Date eventAt) {
        GroupEventType eventType = GroupEventType.from(pigType);
        if (eventType == null) {
            log.error("handle group event type not find, farmId:{}, pigType:{}, eventAt:{}", farmId, pigType, eventAt);
            return;
        }

        switch (eventType) {
            case MOVE_IN:
                handleGroupLiveStock(orgId, farmId, pigType, eventAt);
                break;
            case CHANGE:
                handleGroupLiveStock(orgId, farmId, pigType, eventAt);
                handleChange(orgId, pigType, eventAt);
                break;
            case TRANS_GROUP:
                handleGroupLiveStock(orgId, farmId, pigType, eventAt);
                break;
            case TURN_SEED:
                handleGroupLiveStock(orgId, farmId, pigType, eventAt);
                break;
            case TRANS_FARM:
                handleGroupLiveStock(orgId, farmId, pigType, eventAt);
                break;
            default:
                break;
        }
    }

    //处理变动事件(暂时不区分死淘和销售了，全部算更方便些)
    private void handleChange(Long farmId, Integer pigType, Date eventAt) {
        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();
        
        getDead(PigType.from(pigType), farmId, startAt, endAt);
        getSale(PigType.from(pigType), farmId, startAt, endAt);
    }

    //更新存栏相关的统计
    private void handleGroupLiveStock(Long orgId, Long farmId, Integer pigType, Date eventAt) {
        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = Dates.startOfDay(new Date());

        //更新数据库的存栏统计
        doctorPigTypeStatisticWriteService.statisticGroup(orgId, farmId);

        PigType type = PigType.from(pigType);
        if (type == null) {
            log.error("group event pigType({}) not support!", pigType);
            return;
        }

        //更新到今天的存栏
        while (!startAt.after(endAt)) {
            //查询startAt 这条的日报是否存在，如果已经初始化过了，则不做处理
            if (!doctorDailyReportCache.reportIsFullInit(farmId, startAt)) {
                getLiveStock(type, farmId, startAt);
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
