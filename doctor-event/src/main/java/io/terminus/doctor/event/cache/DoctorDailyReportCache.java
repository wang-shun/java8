package io.terminus.doctor.event.cache;

import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dao.redis.DailyReportHistoryDao;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDeliverDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 日报统计缓存
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Slf4j
@Component
public class DoctorDailyReportCache {

    private final DoctorKpiDao doctorKpiDao;
    private final DoctorPigTypeStatisticDao doctorPigTypeStatisticDao;
    private final DailyReportHistoryDao dailyReportHistoryDao;
    private final DailyReport2UpdateDao dailyReport2UpdateDao;

    @Autowired
    public DoctorDailyReportCache(DoctorKpiDao doctorKpiDao, DailyReportHistoryDao dailyReportHistoryDao,
                                  DoctorPigTypeStatisticDao doctorPigTypeStatisticDao,
                                  DailyReport2UpdateDao dailyReport2UpdateDao) {
        this.doctorKpiDao = doctorKpiDao;
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
        this.dailyReportHistoryDao = dailyReportHistoryDao;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
    }

    /**
     * report put 到redis, 覆盖原先的report
     * @param farmId 猪场id
     * @param date   统计日期
     * @param report 日报统计
     */
    public void putDailyReport(Long farmId, Date date, DoctorDailyReportDto report) {
        dailyReportHistoryDao.saveDailyReport(report, farmId, date);
    }

    /**
     * 更新redis中的日报
     * @param reportDto 猪日报
     * @param date 日报的日期
     */
    public void putDailyPigReport(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        log.info("put daily report pig dto:{}", reportDto);
        Date startAt = Dates.startOfDay(date);
        Date endAt = Dates.startOfDay(new Date());
        DoctorDailyReportDto redisDto = dailyReportHistoryDao.getDailyReportWithRedis(farmId, startAt);
        log.info("put daily report redis dto:{}", redisDto);
        if (redisDto != null) {
            redisDto.setPig(reportDto);
            dailyReportHistoryDao.saveDailyReport(redisDto, farmId, startAt);
        }
        dailyReport2UpdateDao.saveDailyReport2Update(startAt, farmId);

        //第一天已经算过了, 不用重新算
        startAt = new DateTime(startAt).plusDays(1).toDate();

        //更新猪存栏
        while (!startAt.after(endAt)) {
            DoctorDailyReportDto everyRedis = dailyReportHistoryDao.getDailyReportWithRedis(farmId, startAt);

            //存栏
            DoctorLiveStockDailyReport liveStock = everyRedis.getLiveStock();
            liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(farmId, startAt));    //产房母猪
            liveStock.setPeihuaiSow(doctorKpiDao.realTimeLiveStockSow(farmId, startAt) - liveStock.getBuruSow());    //配怀 = 总存栏 - 产房母猪
            liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
            liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(farmId, startAt));            //公猪

            everyRedis.setLiveStock(liveStock);
            dailyReportHistoryDao.saveDailyReport(everyRedis, farmId, startAt);
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
    }

    /**
     * 更新redis中的日报
     * @param reportDto 猪群日报
     */
    public void putDailyGroupReport(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        Date startAt = Dates.startOfDay(date);
        Date endAt = Dates.startOfDay(new Date());

        DoctorDailyReportDto redisDto = dailyReportHistoryDao.getDailyReportWithRedis(farmId, startAt);
        if (redisDto != null) {
            redisDto.setGroup(reportDto);
            dailyReportHistoryDao.saveDailyReport(redisDto, farmId, startAt);
        }
        dailyReport2UpdateDao.saveDailyReport2Update(startAt, farmId);

        //第一天已经算过了, 不用重新算
        startAt = new DateTime(startAt).plusDays(1).toDate();

        //更新猪群存栏
        while (!startAt.after(endAt)) {
            DoctorDailyReportDto everyRedis = dailyReportHistoryDao.getDailyReportWithRedis(farmId, startAt);
            //存栏
            DoctorLiveStockDailyReport liveStock = everyRedis.getLiveStock();
            liveStock.setHoubeiBoar(doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId, startAt));
            liveStock.setHoubeiSow(doctorKpiDao.realTimeLiveStockHoubeiSow(farmId, startAt));  //后备母猪
            liveStock.setFarrow(doctorKpiDao.realTimeLiveStockFarrow(farmId, startAt));
            liveStock.setNursery(doctorKpiDao.realTimeLiveStockNursery(farmId, startAt));
            liveStock.setFatten(doctorKpiDao.realTimeLiveStockFatten(farmId, startAt));

            everyRedis.setLiveStock(liveStock);
            dailyReportHistoryDao.saveDailyReport(everyRedis, farmId, startAt);
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
    }

    /**
     * 清理所有redis中的日报
     */
    public void clearAllReport() {
        dailyReportHistoryDao.deleteDailyReport();
    }

    /**
     * 清理指定猪场在指定日期的缓存
     * @param farmId 猪场
     * @param date 日期
     */
    public void clearFarmReport(Long farmId, Date date) {
        dailyReportHistoryDao.deleteDailyReport(farmId, date);
    }

    //实时Sql查询某猪场的日报统计
    public DoctorDailyReportDto initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        Date startAt = Dates.startOfDay(date);
        Date endAt = DateUtil.getDateEnd(new DateTime(date)).toDate();

        log.info("init daily report farmId:{}, date:{}", farmId, date);
        DoctorDailyReportDto report = new DoctorDailyReportDto();

        //妊娠检查
        DoctorCheckPregDailyReport checkPreg = new DoctorCheckPregDailyReport();
        checkPreg.setPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));
        checkPreg.setNegative(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));
        checkPreg.setFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));
        checkPreg.setLiuchan(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));

        //死淘
        DoctorDeadDailyReport dead = new DoctorDeadDailyReport();
        dead.setBoar(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));
        dead.setSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));
        dead.setFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));
        dead.setNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));
        dead.setFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));
        dead.setHoubei(doctorKpiDao.getDeadHoubei(farmId, startAt, endAt));

        //分娩
        DoctorDeliverDailyReport deliver = new DoctorDeliverDailyReport();
        deliver.setNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));
        deliver.setLive(doctorKpiDao.getDeliveryLive(farmId, startAt, endAt));
        deliver.setHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));
        deliver.setWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));
        deliver.setBlack(doctorKpiDao.getDeliveryDeadBlackMuJi(farmId, startAt, endAt));
        deliver.setAvgWeight(doctorKpiDao.getFarrowWeightAvg(farmId, startAt, endAt));

        //配种
        DoctorMatingDailyReport mating = new DoctorMatingDailyReport();
        mating.setHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));
        mating.setPregCheckResultYing(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));
        mating.setDuannai(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));
        mating.setFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));
        mating.setLiuchan(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));

        //销售
        DoctorSaleDailyReport sale = new DoctorSaleDailyReport();
        sale.setBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));
        sale.setSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));
        sale.setNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));
        sale.setFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));
        sale.setFattenPrice(doctorKpiDao.getGroupSaleFattenPrice(farmId, startAt, endAt));
        sale.setBasePrice10(doctorKpiDao.getGroupSaleBasePrice10(farmId, startAt, endAt));
        sale.setBasePrice15(doctorKpiDao.getGroupSaleBasePrice15(farmId, startAt, endAt));

        //断奶
        DoctorWeanDailyReport wean = new DoctorWeanDailyReport();
        wean.setCount(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));
        wean.setWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));
        wean.setNest(doctorKpiDao.getWeanSow(farmId, startAt, endAt));
        wean.setAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));

        //个体管理母猪总存栏
        report.setSowCount(doctorKpiDao.realTimeLiveStockSow(farmId, startAt));

        //存栏
        DoctorLiveStockDailyReport liveStock = new DoctorLiveStockDailyReport();
        liveStock.setHoubeiBoar(doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId, startAt));
        liveStock.setHoubeiSow(doctorKpiDao.realTimeLiveStockHoubeiSow(farmId, startAt));  //后备母猪
        liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(farmId, startAt));    //产房母猪
        liveStock.setPeihuaiSow(report.getSowCount() - liveStock.getBuruSow());            //配怀 = 总存栏 - 产房母猪
        liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
        liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(farmId, startAt));            //公猪
        liveStock.setFarrow(doctorKpiDao.realTimeLiveStockFarrow(farmId, startAt));
        liveStock.setNursery(doctorKpiDao.realTimeLiveStockNursery(farmId, startAt));
        liveStock.setFatten(doctorKpiDao.realTimeLiveStockFatten(farmId, startAt));

        report.setCheckPreg(checkPreg);
        report.setDead(dead);
        report.setDeliver(deliver);
        report.setMating(mating);
        report.setSale(sale);
        report.setWean(wean);
        report.setLiveStock(liveStock);
        report.setFarmId(farmId);
        report.setSumAt(startAt);
        return report;
    }

    //实时查询全部猪场猪和猪群的日报统计
    public List<DoctorDailyReportDto> initDailyReportByDate(Date date) {
        return doctorPigTypeStatisticDao.findAll().stream()
                .map(p -> initDailyReportByFarmIdAndDate(p.getFarmId(), date))
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    private static class FarmDate {
        private Long farmId;
        private Date date;
    }
}
