package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyGroupDao;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.common.DoctorLiveStockChangeCommonReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@Component
public class DoctorDailyReportManager {

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyReportCache doctorDailyReportCache;
    private final DoctorKpiDao doctorKpiDao;
    private final DoctorDailyGroupDao doctorDailyGroupDao;

    @Autowired
    public DoctorDailyReportManager(DoctorDailyReportDao doctorDailyReportDao,
                                    DoctorDailyReportCache doctorDailyReportCache,
                                    DoctorKpiDao doctorKpiDao,
                                    DoctorDailyGroupDao doctorDailyGroupDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
        this.doctorKpiDao = doctorKpiDao;
        this.doctorDailyGroupDao = doctorDailyGroupDao;
    }
    /**
     * 实时计算日报后, 创建之
     * @param farmId 猪场id
     * @param sumAt  统计日期
     */
    @Transactional
    public void realTimeDailyReports(Long farmId, Date sumAt) {
        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
        report.setSumAt(DateUtil.toDateString(sumAt));

        DoctorDailyReportDto dto = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, sumAt);
//        report.setSowCount(dto.getSowCount());                      //母猪总存栏
//        report.setFarrowCount(dto.getLiveStock().getFarrow());      //产房仔猪
//        report.setNurseryCount(dto.getLiveStock().getNursery());    //保育猪
//        report.setFattenCount(dto.getLiveStock().getFatten());      //育肥猪
//        report.setHoubeiCount(dto.getLiveStock().getHoubei());      //后备猪
//        report.setReportData(dto);
//        doctorDailyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
//        doctorDailyReportDao.create(report);
    }

    /**
     * 获取猪群某一天记录, 没有这初始化
     * @param groupId 猪群id
     * @param sumAt 统计日期
     * @return 猪群记录
     */
    public DoctorDailyGroup findByGroupIdAndSumAt(Long groupId, Date sumAt) {
        DoctorDailyGroup doctorDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(groupId, sumAt);
        if (isNull(doctorDailyGroup)) {
            doctorDailyGroup = new DoctorDailyGroup();
            doctorDailyGroup.setStart(doctorKpiDao.realTimeLivetockGroup(groupId, sumAt));
            doctorDailyGroup.setEnd(doctorDailyGroup.getStart());
            doctorDailyGroup.setSumAt(sumAt);
        }
        return doctorDailyGroup;
    }

    /**
     * 有则更新,无责创建
     * @param dailyGroup 猪群日记录
     */
    public void createOrUpdateDailyGroup (DoctorDailyGroup dailyGroup) {
        if (isNull(dailyGroup.getId())) {
            doctorDailyGroupDao.create(dailyGroup);
        } else {
            doctorDailyGroupDao.update(dailyGroup);
        }
    }
    /**
     * 生成某一天报表
     * @param farmId  猪场id
     * @param date 日期
     * @return
     */
    public Boolean createOrUpdateReport(Long farmId, Date date) {
        DoctorDailyReport newReport = null;
        try {
            newReport = getdoctorDailyReport(farmId, date, DateUtil.getDateEnd(new DateTime(date)).toDate());
            newReport = caculateIndicator(newReport);
            DoctorDailyReport oldDayReport = doctorDailyReportDao.findByFarmIdAndSumAt(farmId, date);
            if (!Objects.isNull(oldDayReport)) {
                newReport.setId(oldDayReport.getId());
                doctorDailyReportDao.update(newReport);
                return Boolean.TRUE;
            }
            doctorDailyReportDao.create(newReport);
        } catch (Exception e) {
            log.info("create or update daily report failed, report: {}, cause: {}", newReport, Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private DoctorDailyReport getdoctorDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorDailyReport doctorDailyReport = new DoctorDailyReport();
        doctorDailyReport.setFarmId(farmId);
        doctorDailyReport.setSumAt(DateUtil.toDateString(startAt));
        doctorDailyReport.setSowPh(doctorKpiDao.realTimeLiveStockPHSow(farmId, startAt));
        doctorDailyReport.setSowCf(doctorKpiDao.realTimeLiveStockFarrowSow(farmId, startAt));
        //母猪存栏变化
        doctorDailyReport.setSowStart(doctorKpiDao.realTimeLiveStockSow(farmId, new DateTime(startAt).minusDays(1).toDate()));
        doctorDailyReport.setSowIn(doctorKpiDao.getInSow(farmId, startAt, endAt));
        doctorDailyReport.setSowDead(doctorKpiDao.getDeadSow(farmId, startAt, endAt));
        doctorDailyReport.setSowWeedOut(doctorKpiDao.getWeedOutSow(farmId, startAt, endAt));
        doctorDailyReport.setSowSale(doctorKpiDao.getSaleSow(farmId, startAt, endAt));
        doctorDailyReport.setSowOtherOut(doctorKpiDao.getOtherOutSow(farmId, startAt, endAt));
        doctorDailyReport.setSowEnd(doctorKpiDao.realTimeLiveStockSow(farmId, startAt));
        //产房母猪存栏变化
        doctorDailyReport.setSowCfStart(doctorKpiDao.realTimeLiveStockFarrowSow(farmId, new DateTime(startAt).minusDays(1).toDate()));
        doctorDailyReport.setSowCfIn(doctorKpiDao.getMonthlyLiveStockChangeWeanIn(farmId, startAt, endAt));
        doctorDailyReport.setSowCfDead(doctorKpiDao.getSowCfDead(farmId, startAt, endAt));
        doctorDailyReport.setSowCfWeedOut(doctorKpiDao.getSowCfWeedOut(farmId, startAt, endAt));
        doctorDailyReport.setSowCfWeanOut(doctorKpiDao.getMonthlyLiveStockChangeWeanIn(farmId, startAt, endAt));
        //配怀母猪存栏变化
        doctorDailyReport.setSowPhStart(EventUtil.minusInt(doctorDailyReport.getSowStart(), doctorDailyReport.getSowCfStart()));
        doctorDailyReport.setSowPhWeanIn(doctorDailyReport.getSowCfWeanOut());
        doctorDailyReport.setSowPhToCf(doctorKpiDao.getMonthlyLiveStockChangeToFarrow(farmId, startAt, endAt));
        doctorDailyReport.setSowPhInFarmIn(doctorKpiDao.getMonthlyLiveStockChangeSowIn(farmId, startAt, endAt));
        doctorDailyReport.setSowPhDead(doctorKpiDao.getSowPhDead(farmId, startAt, endAt));
        doctorDailyReport.setSowPhWeedOut(doctorKpiDao.getSowPhWeedOut(farmId, startAt, endAt));
        //公猪存栏变化
        doctorDailyReport.setBoarStart(doctorKpiDao.realTimeLiveStockBoar(farmId, new DateTime(startAt).minusDays(1).toDate()));
        doctorDailyReport.setBoarIn(doctorKpiDao.getInBoar(farmId, startAt, endAt));
        doctorDailyReport.setBoarDead(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));
        doctorDailyReport.setBoarWeedOut(doctorKpiDao.getWeedOutBoar(farmId, startAt, endAt));
        doctorDailyReport.setBoarSale(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));
        doctorDailyReport.setBoarOtherOut(doctorKpiDao.getOtherOutBoar(farmId, startAt, endAt));
        doctorDailyReport.setBoarEnd(doctorKpiDao.realTimeLiveStockBoar(farmId, startAt));
        //配种情况
        doctorDailyReport.setMateHb(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));
        doctorDailyReport.setMateDn(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));
        doctorDailyReport.setMateFq(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));
        doctorDailyReport.setMateLc(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));
        doctorDailyReport.setMateYx(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));
        //妊娠检查情况
        doctorDailyReport.setPregPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));
        doctorDailyReport.setPregNegative(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));
        doctorDailyReport.setPregFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));
        doctorDailyReport.setPregLiuchan(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));
        //分娩情况
        doctorDailyReport.setFarrowNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));
        doctorDailyReport.setFarrowAll(doctorKpiDao.getDeliveryAll(farmId, startAt, endAt));
        doctorDailyReport.setFarrowLive(doctorKpiDao.getDeliveryLive(farmId, startAt, endAt));
        doctorDailyReport.setFarrowHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));
        doctorDailyReport.setFarrowWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));
        doctorDailyReport.setFarrowDead(doctorKpiDao.getDeliveryDead(farmId, startAt, endAt));
        doctorDailyReport.setFarrowJx(doctorKpiDao.getDeliveryJx(farmId, startAt, endAt));
        doctorDailyReport.setFarrowMny(doctorKpiDao.getDeliveryMny(farmId, startAt, endAt));
        doctorDailyReport.setFarrowBlack(doctorKpiDao.getDeliveryBlack(farmId, startAt, endAt));
        doctorDailyReport.setFarrowSjmh(doctorKpiDao.getDeliveryDeadBlackMuJi(farmId, startAt, endAt));
        doctorDailyReport.setFarrowWeight(doctorKpiDao.getFarrowWeight(farmId, startAt, endAt));
        doctorDailyReport.setFarrowAvgWeight(doctorKpiDao.getFarrowWeightAvg(farmId, startAt, endAt));
        //断奶情况
        doctorDailyReport.setWeanNest(doctorKpiDao.getWeanSow(farmId, startAt, endAt));
        doctorDailyReport.setWeanCount(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));
        doctorDailyReport.setWeanAvgWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));
        doctorDailyReport.setWeanDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));
        return doctorDailyReport;
    }

    private DoctorDailyReport caculateIndicator(DoctorDailyReport doctorDailyReport){
        Long farmId = doctorDailyReport.getFarmId();
        Date startAt = DateUtil.toDate(doctorDailyReport.getSumAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(startAt)).toDate();

        DoctorLiveStockChangeCommonReport changeCountReport = doctorKpiDao.getMonthlyLiveStockChangeFeedCount(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport changeAmountReport = doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId, startAt, endAt);

        doctorDailyReport.setFattenPrice(doctorKpiDao.getGroupSaleFattenPrice(farmId, startAt, endAt));
        doctorDailyReport.setBasePrice10(doctorKpiDao.getGroupSaleBasePrice10(farmId, startAt, endAt));
        doctorDailyReport.setBasePrice15(doctorKpiDao.getGroupSaleBasePrice15(farmId, startAt, endAt));

        doctorDailyReport.setSowPhFeed(changeCountReport.getPeiHuaiFeedCount());
        doctorDailyReport.setSowPhFeedAmount(changeAmountReport.getPeiHuaiFeedAmount());
        doctorDailyReport.setSowPhMedicineAmount(changeAmountReport.getPeiHuaiDrugAmount());
        doctorDailyReport.setSowPhVaccinationAmount(changeAmountReport.getPeiHuaiVaccineAmount());
        doctorDailyReport.setSowPhConsumableAmount(changeAmountReport.getPeiHuaiConsumerAmount());

        doctorDailyReport.setSowCfFeed(changeCountReport.getFarrowSowFeedCount());
        doctorDailyReport.setSowCfFeedAmount(changeAmountReport.getFarrowSowFeedAmount());
        doctorDailyReport.setSowCfMedicineAmount(changeAmountReport.getFarrowSowDrugAmount());
        doctorDailyReport.setSowCfVaccinationAmount(changeAmountReport.getFarrowSowVaccineAmount());
        doctorDailyReport.setSowCfConsumableAmount(changeAmountReport.getFarrowSowConsumerAmount());

        doctorDailyReport.setFarrowFeed(changeCountReport.getFarrowFeedCount());
        doctorDailyReport.setFarrowFeedAmount(changeAmountReport.getFarrowFeedAmount());
        doctorDailyReport.setFarrowMedicineAmount(changeAmountReport.getFarrowDrugAmount());
        doctorDailyReport.setFarrowVaccinationAmount(changeAmountReport.getFarrowVaccineAmount());
        doctorDailyReport.setFarrowConsumableAmount(changeAmountReport.getFarrowConsumerAmount());

        doctorDailyReport.setNurseryFeed(changeCountReport.getNurseryFeedCount());
        doctorDailyReport.setNurseryFeedAmount(changeAmountReport.getNurseryFeedAmount());
        doctorDailyReport.setNurseryMedicineAmount(changeAmountReport.getNurseryDrugAmount());
        doctorDailyReport.setNurseryVaccinationAmount(changeAmountReport.getNurseryVaccineAmount());
        doctorDailyReport.setNurseryConsumableAmount(changeAmountReport.getNurseryConsumerAmount());

        doctorDailyReport.setFattenFeed(changeCountReport.getFattenFeedCount());
        doctorDailyReport.setFattenFeedAmount(changeAmountReport.getFattenFeedAmount());
        doctorDailyReport.setFattenMedicineAmount(changeAmountReport.getFattenDrugAmount());
        doctorDailyReport.setFattenVaccinationAmount(changeAmountReport.getFattenVaccineAmount());
        doctorDailyReport.setFattenConsumableAmount(changeAmountReport.getFattenConsumerAmount());

        doctorDailyReport.setHoubeiFeed(changeCountReport.getHoubeiFeedCount());
        doctorDailyReport.setHoubeiFeedAmount(changeAmountReport.getHoubeiFeedAmount());
        doctorDailyReport.setHoubeiMedicineAmount(changeAmountReport.getHoubeiDrugAmount());
        doctorDailyReport.setHoubeiVaccinationAmount(changeAmountReport.getHoubeiVaccineAmount());
        doctorDailyReport.setHoubeiConsumableAmount(changeAmountReport.getHoubeiConsumerAmount());

        return doctorDailyReport;
    }
}
