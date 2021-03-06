package io.terminus.doctor.event.cache;

import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigTypeStatisticDao;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dto.report.daily.*;
import io.terminus.doctor.event.model.DoctorDailyReport;
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
    private final DailyReport2UpdateDao dailyReport2UpdateDao;
    private final DoctorDailyReportDao doctorDailyReportDao;

    @Autowired
    public DoctorDailyReportCache(DoctorKpiDao doctorKpiDao,
                                  DoctorPigTypeStatisticDao doctorPigTypeStatisticDao,
                                  DailyReport2UpdateDao dailyReport2UpdateDao,
                                  DoctorDailyReportDao doctorDailyReportDao) {
        this.doctorKpiDao = doctorKpiDao;
        this.doctorPigTypeStatisticDao = doctorPigTypeStatisticDao;
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
        this.doctorDailyReportDao = doctorDailyReportDao;
    }

    /**
     * 从redis中取report，如果没有取数据库的
     * @param farmId  猪场id
     * @param date    日期
     * @return  日报
     */
    public DoctorDailyReport getDailyReport(Long farmId, Date date) {
//        return doctorDailyReportDao.findByFarmIdAndSumAt(farmId, Dates.startOfDay(date));
        return null;
    }

    public DoctorDailyReportDto getDailyReportDto(Long farmId, Date date) {
//        DoctorDailyReport report = getDailyReport(farmId, date);
//        if (report == null || report.getReportData() == null) {
//            return null;
//        }
//        return report.getReportData();
        return null;
    }

    /**
     * report put 到MySQL
     */
    public void putDailyReportToMySQL(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        saveEventAtWhenLiveStock(farmId, date);
//        doctorDailyReportDao.updateByFarmIdAndSumAt(makeDailyReport(farmId, date, reportDto));
    }

    //每次创建今天之前的事件，需要记录事件时间，晚上的job会扫到这个时间，然后刷一遍日报
    private void saveEventAtWhenLiveStock(Long farmId, Date eventAt) {
        Date startAt = Dates.startOfDay(eventAt);
        Date endAt = Dates.startOfDay(new Date());
        if (!startAt.equals(endAt)) {
            dailyReport2UpdateDao.saveDailyReport2Update(startAt, farmId);
        }
    }

    //实时Sql查询某猪场的日报统计
    public DoctorDailyReportDto initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        Date startAt = Dates.startOfDay(date);
        Date endAt = DateUtil.getDateEnd(new DateTime(date)).toDate();
        Long orgId = doctorKpiDao.getOrgIdByFarmId(farmId);

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
        dead.setWeedOutBoar(doctorKpiDao.getWeedOutBoar(farmId, startAt, endAt));
        dead.setSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));
        dead.setWeedOutSow(doctorKpiDao.getWeedOutSow(farmId, startAt, endAt));
        dead.setFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));
        dead.setWeedOutFarrow(doctorKpiDao.getWeedOutFarrow(farmId, startAt, endAt));
        dead.setNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));
        dead.setWeedOutNursery(doctorKpiDao.getWeedOutNursery(farmId, startAt, endAt));
        dead.setFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));
        dead.setWeedOutFatten(doctorKpiDao.getWeedOutFatten(farmId, startAt, endAt));
        dead.setHoubei(doctorKpiDao.getDeadHoubei(farmId, startAt, endAt));
        dead.setWeedOutHoubei(doctorKpiDao.getWeedOutHoubei(farmId, startAt, endAt));

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
        sale.setHoubei(doctorKpiDao.getSaleHoubei(farmId, startAt, endAt));
        sale.setFattenPrice(doctorKpiDao.getGroupSaleFattenPrice(farmId, startAt, endAt));
        sale.setBasePrice10(doctorKpiDao.getGroupSaleBasePrice10(farmId, startAt, endAt));
        sale.setBasePrice15(doctorKpiDao.getGroupSaleBasePrice15(farmId, startAt, endAt));

        //断奶
        DoctorWeanDailyReport wean = new DoctorWeanDailyReport();
        wean.setCount(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));
        wean.setWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));
        wean.setNest(doctorKpiDao.getWeanSow(farmId, startAt, endAt));
        wean.setAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));
        wean.setFarrowChgFarm(doctorKpiDao.getFarrowChgFarmCount(farmId, startAt, endAt));
        wean.setFarrowToNursery(doctorKpiDao.getFarrowToNursery(farmId, startAt, endAt));
        wean.setFarrowSale(doctorKpiDao.getFarrowSaleCount(farmId, startAt, endAt));

        //个体管理母猪总存栏
//        report.setSowCount(doctorKpiDao.realTimeLiveStockSow(farmId, startAt));

        //存栏
        DoctorLiveStockDailyReport liveStock = new DoctorLiveStockDailyReport();
        liveStock.setHoubeiBoar(doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId, startAt));
        liveStock.setHoubeiSow(doctorKpiDao.realTimeLiveStockHoubeiSow(farmId, startAt));  //后备母猪
        liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(orgId,farmId, startAt));    //产房母猪
//        liveStock.setPeihuaiSow(report.getSowCount() - liveStock.getBuruSow());            //配怀 = 总存栏 - 产房母猪
        liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
        liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(farmId, startAt));            //公猪
        liveStock.setFarrow(doctorKpiDao.realTimeLiveStockFarrow(farmId, startAt));
        liveStock.setNursery(doctorKpiDao.realTimeLiveStockNursery(farmId, startAt));
        liveStock.setFatten(doctorKpiDao.realTimeLiveStockFatten(farmId, startAt));

        //后备猪
        DoctorHoubeiReport houbei = new DoctorHoubeiReport();
        houbei.setHoubeiCount(doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId,startAt));
        houbei.setHoubeiIn(doctorKpiDao.getMonthlyLiveStockChangeIn(farmId,startAt,endAt).getPeiHuaiBegin());
        houbei.setChangeSeed(doctorKpiDao.getMonthlyLiveStockChangeToSeed(farmId,startAt,endAt));
        houbei.setDead(doctorKpiDao.getDeadHoubei(farmId,startAt,endAt));
        houbei.setSales(doctorKpiDao.getSaleHoubei(farmId,startAt,endAt));
        houbei.setEliminate(doctorKpiDao.getWeedOutHoubei(farmId,startAt,endAt));
        houbei.setChangeFarm(doctorKpiDao.getMonthlyLiveStockChangeGroupHoubeiNumber(farmId,startAt,endAt));
        houbei.setOther(doctorKpiDao.getMonthlyLiveStockChangeGroupHoubeiOtherNumber(farmId,startAt,endAt));
        houbei.setMaterial(doctorKpiDao.getMonthlyLiveStockChangeFeedCount(farmId,startAt,endAt).getHoubeiFeedCount());
        houbei.setMedicinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getHoubeiDrugAmount());
        houbei.setConsumablesPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getHoubeiConsumerAmount());
        houbei.setVaccinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getHoubeiVaccineAmount());
        houbei.setMaterialPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getHoubeiFeedAmount());

        //育肥猪
        DoctorFatteningReport fattening = new DoctorFatteningReport();
        fattening.setFattenCount(doctorKpiDao.realTimeLiveStockFatten(farmId,startAt));
        fattening.setFattenIn(doctorKpiDao.getMonthlyLiveStockChangeIn(farmId,startAt,endAt).getFattenIn());
        fattening.setDead(doctorKpiDao.getDeadFatten(farmId,startAt,endAt));
        fattening.setEliminate(doctorKpiDao.getWeedOutFatten(farmId,startAt,endAt));
        fattening.setSales(doctorKpiDao.getSaleFatten(farmId,startAt,endAt));
        fattening.setChangeHoubei(doctorKpiDao.getMonthlyLiveStockChangeToHoubei(farmId,startAt,endAt));
        fattening.setChangeFarm(doctorKpiDao.getMonthlyLiveStockChangeGroupFattenNumber(farmId,startAt,endAt));
        fattening.setOther(doctorKpiDao.getMonthlyLiveStockChangeGroupFattenOtherNumber(farmId,startAt,endAt));
        fattening.setMaterial(doctorKpiDao.getMonthlyLiveStockChangeFeedCount(farmId,startAt,endAt).getFarrowFeedCount());
        fattening.setMedicinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getFattenDrugAmount());
        fattening.setConsumablesPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getFattenConsumerAmount());
        fattening.setVaccinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getFattenVaccineAmount());
        fattening.setMaterialPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getFattenFeedAmount());

        //保育猪
        DoctorNurseryReport nursery = new DoctorNurseryReport();
        nursery.setNursery(doctorKpiDao.realTimeLiveStockNursery(farmId,startAt));
        nursery.setNurseryIn(doctorKpiDao.getMonthlyLiveStockChangeIn(farmId,startAt,endAt).getNurseryIn());
        nursery.setDead(doctorKpiDao.getDeadNursery(farmId,startAt,endAt));
        nursery.setEliminate(doctorKpiDao.getWeedOutNursery(farmId,startAt,endAt));
        nursery.setSales(doctorKpiDao.getSaleNursery(farmId,startAt,endAt));
        nursery.setChangeFattening(doctorKpiDao.getMonthlyLiveStockChangeToFatten(farmId,startAt,endAt));
        nursery.setChangeFarm(doctorKpiDao.getMonthlyLiveStockChangeGroupNuseryNumber(farmId,startAt,endAt));
        nursery.setOther(doctorKpiDao.getMonthlyLiveStockChangeGroupNuseryOtherNumber(farmId,startAt,endAt));
        nursery.setMaterial(doctorKpiDao.getMonthlyLiveStockChangeFeedCount(farmId,startAt,endAt).getNurseryFeedCount());
        nursery.setMedicinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getNurseryDrugAmount());
        nursery.setConsumablesPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getNurseryConsumerAmount());
        nursery.setVaccinePrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getNurseryVaccineAmount());
        nursery.setMaterialPrice(doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId,startAt,endAt).getNurseryFeedAmount());



//        report.setCheckPreg(checkPreg);
//        report.setDead(dead);
//        report.setDeliver(deliver);
//        report.setMating(mating);
//        report.setSale(sale);
//
//        report.setHoubei(houbei);
//        report.setNursery(nursery);
//        report.setFattening(fattening);
//
//        report.setWean(wean);
//        report.setLiveStock(liveStock);
//        report.setFarmId(farmId);
//        report.setSumAt(startAt);
        return report;
    }

    //实时查询全部猪场猪和猪群的日报统计
    public List<DoctorDailyReportDto> initDailyReportByDate(Date date) {
        return doctorPigTypeStatisticDao.findAll().stream()
                .map(p -> initDailyReportByFarmIdAndDate(p.getFarmId(), date))
                .collect(Collectors.toList());
    }

    //日报是否已被全量更新
    public boolean reportIsFullInit(Long farmId, Date date) {
        DoctorDailyReport report = this.getDailyReport(farmId, date);
//        if (report == null || report.getReportData() == null) {
//            DoctorDailyReportDto reportDto = this.initDailyReportByFarmIdAndDate(farmId, date);
//            doctorDailyReportDao.create(makeDailyReport(farmId, date, reportDto));
//            return true;
//        }
        return false;
    }

    //拼装dailReport
    private DoctorDailyReport makeDailyReport(Long farmId, Date date, DoctorDailyReportDto reportDto) {
        DoctorDailyReport report = new DoctorDailyReport();
//        report.setFarmId(farmId);
//        report.setSumAt(Dates.startOfDay(date));
//        report.setSowCount(reportDto.getSowCount());
//        report.setFarrowCount(reportDto.getLiveStock().getFarrow());
//        report.setNurseryCount(reportDto.getLiveStock().getNursery());
//        report.setFattenCount(reportDto.getLiveStock().getFatten());
//        report.setReportData(reportDto);
        return report;
    }
}
