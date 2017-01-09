package io.terminus.doctor.event.event;

import com.google.common.base.MoreObjects;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪事件监听器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/28
 */
@Slf4j
@Component
@SuppressWarnings("unused")
public class DoctorPigEventListener implements EventListener {

    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Autowired
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    private DoctorDailyReportCache doctorDailyReportCache;

    @Autowired
    private DoctorDailyReportDao doctorDailyReportDao;

    public void handlePigEvent() {
        // TODO: 2017/1/9  
    }
    
    /**
     * 监听猪相关事件，处理下日报统计
     */
    @AllowConcurrentEvents
    @Subscribe
    public void handle(Long orgId, Long farmId, DoctorPigPublishDto event) {
        PigEvent type = PigEvent.from(event.getEventType());
        if (type == null) {
            log.error("handle pig event type not find, farmId:{}, event info:{}", farmId, event);
            return;
        }

        switch (type) {
            case CHG_FARM:
                handleLiveStockReport(orgId, farmId, event.getEventAt(), event.getKind());
                break;
            case REMOVAL:
                handleLiveStockReport(orgId, farmId, event.getEventAt(), event.getKind());
                handleSaleAndDead(farmId, event.getEventAt());
                break;
            case ENTRY:
                handleLiveStockReport(orgId, farmId, event.getEventAt(), event.getKind());
                break;
            case MATING:
                handleMate(farmId, event.getEventAt(), event.getMateType());
                break;
            case PREG_CHECK:
                handlePregCheck(farmId, event.getEventAt(), event.getPregCheckResult());
                break;
            case FARROWING:
                handleFarrow(farmId, event.getEventAt());
                break;
            case WEAN:
                handleWean(farmId, event.getEventAt());
                break;
            default:
                break;
        }
    }

    //处理配种
    private void handleMate(Long farmId, Date eventAt, Integer mateType) {
        log.info("handle handleMate, farmId:{}, eventAt:{}, mateType:{}", farmId, eventAt, mateType);

        if (doctorDailyReportCache.reportIsFullInit(farmId, eventAt)) {
            return;
        }

        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();

        //不同的配种来源
        DoctorMatingType matingType = DoctorMatingType.from(mateType);
        if (matingType == null) {
            log.error("handle pig mate event, but matetype unsupport! farmId:{}", farmId);
            return;
        }
        switch (matingType) {
            case HP:
                int hp = doctorKpiDao.firstMatingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoHP = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoHP.getMating().setHoubei(hp);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoHP);
                break;
            case DP:
                int dp = doctorKpiDao.weanMatingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoDP = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoDP.getMating().setDuannai(dp);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoDP);
                break;
            case YP:
                int yp = doctorKpiDao.yinMatingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoYP = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoYP.getMating().setPregCheckResultYing(yp);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoYP);
                break;
            case FP:
                int fp = doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoFP = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFP.getMating().setFanqing(fp);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFP);
                break;
            default:
                int lp = doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoLP = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoLP.getMating().setLiuchan(lp);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoLP);
                break;
        }
    }

    //处理妊检
    private void handlePregCheck(Long farmId, Date eventAt, Integer checkResult) {
        log.info("handle handlePregCheck, farmId:{}, eventAt:{}, mateType:{}", farmId, eventAt, checkResult);

        if (doctorDailyReportCache.reportIsFullInit(farmId, eventAt)) {
            return;
        }

        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();

        PregCheckResult result = PregCheckResult.from(checkResult);
        if (result == null) {
            log.error("handle pig pregcheck event, but checkResult unsupport! farmId:{}", farmId);
            return;
        }
        switch (result) {
            case YANG:
                int yang = doctorKpiDao.checkYangCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoYANG = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoYANG.getCheckPreg().setPositive(yang);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoYANG);
                break;
            case YING:
                int ying = doctorKpiDao.checkYingCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoYING = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoYING.getCheckPreg().setNegative(ying);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoYING);
                break;
            case LIUCHAN:
                int lc = doctorKpiDao.checkYangCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoLIUCHAN = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoLIUCHAN.getCheckPreg().setLiuchan(lc);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoLIUCHAN);
                break;
            case FANQING:
                int fq = doctorKpiDao.checkFanQCounts(farmId, startAt, endAt);
                DoctorDailyReportDto reportDtoFANQING = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
                reportDtoFANQING.getCheckPreg().setFanqing(fq);
                doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDtoFANQING);
                break;
            default:
                break;
        }
    }

    //处理分娩
    private void handleFarrow(Long farmId, Date eventAt) {
        log.info("handle handleFarrow, farmId:{}, eventAt:{}", farmId, eventAt);

        if (doctorDailyReportCache.reportIsFullInit(farmId, eventAt)) {
            return;
        }

        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();

        //取出当天的所有分娩事件，计算一发就好了
        List<DoctorPigEvent> events = doctorPigEventDao.findByFarmIdAndTypeAndDate(farmId, PigEvent.FARROWING.getKey(), startAt, endAt);
        if (!notEmpty(events)) {
            log.error("handle farrow event, but farrow event not found! farmId:{}", farmId);
            return;
        }
        DoctorDailyReportDto reportDto = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
        int live = CountUtil.intStream(events, DoctorPigEvent::getLiveCount).sum();
        reportDto.getDeliver().setNest(events.size());
        reportDto.getDeliver().setLive(live);
        reportDto.getDeliver().setHealth(CountUtil.intStream(events, DoctorPigEvent::getHealthCount).sum());
        reportDto.getDeliver().setWeak(CountUtil.intStream(events, DoctorPigEvent::getWeakCount).sum());
        reportDto.getDeliver().setBlack(CountUtil.intStream(events, e -> MoreObjects.firstNonNull(e.getBlackCount(), 0)
                        + MoreObjects.firstNonNull(e.getDeadCount(), 0)
                        + MoreObjects.firstNonNull(e.getMnyCount(), 0)
                        + MoreObjects.firstNonNull(e.getJxCount(), 0))
                .sum());

        //均重
        double farrowWeiht = CountUtil.doubleStream(events, DoctorPigEvent::getFarrowWeight).sum();
        reportDto.getDeliver().setAvgWeight(live == 0 ? 0 : farrowWeiht/live);

        doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDto);
    }

    //处理断奶
    private void handleWean(Long farmId, Date eventAt) {
        log.info("handle handleWean, farmId:{}, eventAt:{}, mateType:{}", farmId, eventAt);
        if (doctorDailyReportCache.reportIsFullInit(farmId, eventAt)) {
            return;
        }

        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();

        int count = doctorKpiDao.getWeanPiglet(farmId, startAt, endAt);
        double weight = doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt);
        int nest = doctorKpiDao.getWeanSow(farmId, startAt, endAt);
        double age = doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt);

        DoctorDailyReportDto reportDto = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
        reportDto.getWean().setCount(count);
        reportDto.getWean().setWeight(weight);
        reportDto.getWean().setNest(nest);
        reportDto.getWean().setAvgDayAge(age);
        doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDto);
    }

    //跟猪存栏相关的更新 eventAt是最早的一天
    private void handleLiveStockReport(Long orgId, Long farmId, Date eventAt, Integer kind) {
        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = Dates.startOfDay(new Date());

        //更新今天的首页存栏
        doctorPigTypeStatisticWriteService.statisticPig(orgId, farmId, kind);

        //更新到今天的存栏
        while (!startAt.after(endAt)) {
            //查询startAt 这条的日报是否存在，如果已经初始化过了，则不做处理
            if (!doctorDailyReportCache.reportIsFullInit(farmId, startAt)) {
                getLiveStock(kind, farmId, startAt);
            }
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
    }

    private void getLiveStock(Integer sex, Long farmId, Date startAt) {
        log.info("handle getLiveStock, farmId:{}, startAt:{}", farmId, startAt);
        if (Objects.equals(sex, DoctorPig.PIG_TYPE.BOAR.getKey())) {
            int boar = doctorKpiDao.realTimeLiveStockBoar(farmId, startAt);

            DoctorDailyReportDto reportDto = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
            reportDto.getLiveStock().setBoar(boar);
            doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDto);

        } else {
            int buruSow = doctorKpiDao.realTimeLiveStockFarrowSow(farmId, startAt);
            int allSow = doctorKpiDao.realTimeLiveStockSow(farmId, startAt);

            DoctorDailyReportDto reportDto = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
            reportDto.getLiveStock().setBuruSow(buruSow);
            reportDto.getLiveStock().setPeihuaiSow(allSow - buruSow);
            reportDto.getLiveStock().setKonghuaiSow(0);
            doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDto);
        }
    }

    //处理离场类型：死淘或销售，这里就不区分了
    private void handleSaleAndDead(Long farmId, Date eventAt) {
        log.info("handle handleSaleAndDead, farmId:{}, eventAt:{}", farmId, eventAt);

        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = DateUtil.getDateEnd(new DateTime(eventAt)).toDate();

        int deadSow = doctorKpiDao.getDeadSow(farmId, startAt, endAt);
        int deadBoar = doctorKpiDao.getDeadBoar(farmId, startAt, endAt);
        int saleSow = doctorKpiDao.getSaleSow(farmId, startAt, endAt);
        int saleBoar = doctorKpiDao.getSaleBoar(farmId, startAt, endAt);

        DoctorDailyReportDto reportDto = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
        reportDto.getDead().setSow(deadSow);
        reportDto.getDead().setBoar(deadBoar);
        reportDto.getSale().setSow(saleSow);
        reportDto.getSale().setBoar(saleBoar);
        doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, reportDto);
    }
}
