package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorWeeklyReportDao;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.common.DoctorLiveStockChangeCommonReport;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.model.DoctorWeeklyReport;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Component
public class DoctorCommonReportManager {

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;
    private final DoctorWeeklyReportDao doctorWeeklyReportDao;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorCommonReportManager(DoctorMonthlyReportDao doctorMonthlyReportDao,
                                     DoctorWeeklyReportDao doctorWeeklyReportDao,
                                     DoctorKpiDao doctorKpiDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
        this.doctorWeeklyReportDao = doctorWeeklyReportDao;
        this.doctorKpiDao = doctorKpiDao;
    }

    /**
     * 删除sumAt数据, 再批量创建
     *
     * @param monthlyReports 月报
     * @param sumAt          统计日期
     */
    @Transactional
    public void createMonthlyReports(List<DoctorMonthlyReport> monthlyReports, Date sumAt) {
        doctorMonthlyReportDao.deleteBySumAt(sumAt);
        doctorMonthlyReportDao.creates(monthlyReports);
    }

    @Transactional
    public void createMonthlyReport(Long farmId, DoctorMonthlyReport monthlyReport, Date sumAt) {
        doctorMonthlyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorMonthlyReportDao.create(monthlyReport);
    }

    /**
     * 删除sumAt数据, 再批量创建
     *
     * @param weeklyReport 周报
     * @param sumAt        统计日期
     */
    @Transactional
    public void createWeeklyReports(List<DoctorWeeklyReport> weeklyReport, Date sumAt) {
        doctorWeeklyReportDao.deleteBySumAt(sumAt);
        doctorWeeklyReportDao.creates(weeklyReport);
    }

    @Transactional
    public void createWeeklyReport(Long farmId, DoctorWeeklyReport weeklyReport, Date sumAt) {
        doctorWeeklyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorWeeklyReportDao.create(weeklyReport);
    }

    /**
     * 获取存栏变动月报
     */
    public DoctorLiveStockChangeCommonReport getLiveStockChangeReport(Long farmId, Date startAt, Date endAt) {
        DoctorLiveStockChangeCommonReport report = new DoctorLiveStockChangeCommonReport();

        //这里的期初应该是每月1号的0点，也就是前一天的天末，所以要减一天
        DoctorLiveStockChangeCommonReport begin = doctorKpiDao.getMonthlyLiveStockChangeBegin(farmId, new DateTime(startAt).plusDays(-1).toDate());

        DoctorLiveStockChangeCommonReport in = doctorKpiDao.getMonthlyLiveStockChangeIn(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport groupDead = doctorKpiDao.getMonthlyLiveStockChangeGroupDead(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport sowDead = doctorKpiDao.getMonthlyLiveStockChangeSowDead(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport sale = doctorKpiDao.getMonthlyLiveStockChangeSale(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport feedCount = doctorKpiDao.getMonthlyLiveStockChangeFeedCount(farmId, startAt, endAt);
        DoctorLiveStockChangeCommonReport amount = doctorKpiDao.getMonthlyLiveStockChangeMaterielAmount(farmId, startAt, endAt);

        //后备舍
        report.setHoubeiBegin(begin.getHoubeiBegin());                                                      //期初
        report.setHoubeiIn(in.getHoubeiIn());                                                               //转入
        report.setHoubeiToSeed(doctorKpiDao.getMonthlyLiveStockChangeToSeed(farmId, startAt, endAt));       //转种猪
        report.setHoubeiDead(groupDead.getHoubeiDead());                                                    //死淘
        report.setHoubeiSale(sale.getHoubeiSale());                                                         //销售
        report.setHoubeiFeedCount(feedCount.getHoubeiFeedCount());                                          //饲料数量
        report.setHoubeiFeedAmount(amount.getHoubeiFeedAmount());                                           //饲料金额
        report.setHoubeiDrugAmount(amount.getHoubeiDrugAmount());                                           //药品金额
        report.setHoubeiVaccineAmount(amount.getHoubeiVaccineAmount());                                     //疫苗金额
        report.setHoubeiConsumerAmount(amount.getHoubeiConsumerAmount());                                   //易耗品金额

        //配怀舍
        report.setPeiHuaiBegin(begin.getPeiHuaiBegin());                                                    //期初
        report.setPeiHuaiToFarrow(doctorKpiDao.getMonthlyLiveStockChangeToFarrow(farmId, startAt, endAt));  //转产房
        report.setPeiHuaiIn(doctorKpiDao.getMonthlyLiveStockChangeSowIn(farmId, startAt, endAt));           //进场
        report.setPeiHuaiWeanIn(doctorKpiDao.getMonthlyLiveStockChangeWeanIn(farmId, startAt, endAt));      //断奶转入 = 断奶转出
        report.setPeiHuaiDead(sowDead.getPeiHuaiDead());                                                    //死淘
        report.setPeiHuaiFeedCount(feedCount.getPeiHuaiFeedCount());                                        //饲料数量
        report.setPeiHuaiFeedAmount(amount.getPeiHuaiFeedAmount());                                         //饲料金额
        report.setPeiHuaiDrugAmount(amount.getPeiHuaiDrugAmount());                                         //药品金额
        report.setPeiHuaiVaccineAmount(amount.getPeiHuaiVaccineAmount());                                   //疫苗金额
        report.setPeiHuaiConsumerAmount(amount.getPeiHuaiConsumerAmount());                                 //易耗品金额

        //产房母猪
        report.setFarrowSowBegin(begin.getFarrowSowBegin());                                                //期初
        report.setFarrowSowIn(report.getPeiHuaiToFarrow());                                                 //转入 = 转产房
        report.setFarrowSowWeanOut(report.getPeiHuaiWeanIn());                                              //断奶转出 = 断奶转入
        report.setFarrowSowDead(sowDead.getFarrowSowDead());                                                //死淘
        report.setFarrowSowFeedCount(feedCount.getFarrowSowFeedCount());                                    //饲料数量
        report.setFarrowSowFeedAmount(amount.getFarrowSowFeedAmount());                                     //饲料金额
        report.setFarrowSowDrugAmount(amount.getFarrowSowDrugAmount());                                     //药品金额
        report.setFarrowSowVaccineAmount(amount.getFarrowSowVaccineAmount());                               //疫苗金额
        report.setFarrowSowConsumerAmount(amount.getFarrowSowConsumerAmount());                             //易耗品金额

        //产房仔猪
        report.setFarrowBegin(begin.getFarrowBegin());                                                      //期初
        report.setFarrowIn(in.getFarrowIn());                                                               //转入
        report.setFarrowToNursery(doctorKpiDao.getMonthlyLiveStockChangeToNursery(farmId, startAt, endAt)); //转保育
        report.setFarrowDead(groupDead.getFarrowDead());                                                    //死淘
        report.setFarrowSale(sale.getFarrowSale());                                                         //销售
        report.setFarrowFeedCount(feedCount.getFarrowFeedCount());                                          //饲料数量
        report.setFarrowFeedAmount(amount.getFarrowFeedAmount());                                           //饲料金额
        report.setFarrowDrugAmount(amount.getFarrowDrugAmount());                                           //药品金额
        report.setFarrowVaccineAmount(amount.getFarrowVaccineAmount());                                     //疫苗金额
        report.setFarrowConsumerAmount(amount.getFarrowConsumerAmount());                                   //易耗品金额

        //保育猪
        report.setNurseryBegin(begin.getNurseryBegin());                                                    //期初
        report.setNurseryIn(report.getFarrowToNursery());                                                             //转入
        report.setNurseryToFatten(doctorKpiDao.getMonthlyLiveStockChangeToFatten(farmId, startAt, endAt));  //转育肥
        report.setNurseryDead(groupDead.getNurseryDead());                                                  //死淘
        report.setNurserySale(sale.getNurserySale());                                                       //销售
        report.setNurseryFeedCount(feedCount.getNurseryFeedCount());                                        //饲料数量
        report.setNurseryFeedAmount(amount.getNurseryFeedAmount());                                         //饲料金额
        report.setNurseryDrugAmount(amount.getNurseryDrugAmount());                                         //药品金额
        report.setNurseryVaccineAmount(amount.getNurseryVaccineAmount());                                   //疫苗金额
        report.setNurseryConsumerAmount(amount.getNurseryConsumerAmount());                                 //易耗品金额

        //育肥猪
        report.setFattenBegin(begin.getFattenBegin());                                                      //期初
        report.setFattenIn(report.getNurseryToFatten());                                                               //转入
        report.setFattenDead(groupDead.getFattenDead());                                                    //死淘
        report.setFattenSale(sale.getFattenSale());                                                         //销售
        report.setFattenFeedCount(feedCount.getFattenFeedCount());                                          //饲料数量
        report.setFattenFeedAmount(amount.getFattenFeedAmount());                                           //饲料金额
        report.setFattenDrugAmount(amount.getFattenDrugAmount());                                           //药品金额
        report.setFattenVaccineAmount(amount.getFattenVaccineAmount());                                     //疫苗金额
        report.setFattenConsumerAmount(amount.getFattenConsumerAmount());                                   //易耗品金额
        return report;
    }

    private static Date weekStart(Date date) {
        return new DateTime(date).withDayOfWeek(1).withTimeAtStartOfDay().toDate();
    }

    private static Date monthStart(Date date) {
        return new DateTime(date).withDayOfMonth(1).withTimeAtStartOfDay().toDate();
    }

    private static Date monthEnd(Date date) {
        return date; // TODO: 2017/1/11
    }

    private DoctorMonthlyReport getMonthlyReport(Long farmId, Date eventAt) {
        return doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, DateUtil.getMonthEnd(new DateTime(eventAt)).toDate());
    }

    private DoctorWeeklyReport getWeeklyReport(Long farmId, Date eventAt) {
        //return doctorWeeklyReportDao.findByFarmIdAndSumAt(farmId, DateUtil.getwee(new DateTime(eventAt)).toDate());
        // TODO: 2017/1/11
        return new DoctorWeeklyReport();
    }

    //更新存栏变动
    public void updateMonthlyLiveStockChange(Long farmId, Date eventAt) {
        DoctorLiveStockChangeCommonReport report = getLiveStockChangeReport(farmId, monthStart(eventAt), monthEnd(eventAt));

        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        month.getReportDto().setLiveStockChange(report);
        doctorMonthlyReportDao.update(month);
    }

    //更新销售死淘
    public void updateMonthlySaleDead(Long farmId, Date eventAt) {
        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        setSaleDead(month.getReportDto(), farmId, monthStart(eventAt), eventAt);
        doctorMonthlyReportDao.update(month);
    }

    //更新胎次分布，品类分布
    public void updateMonthlyParityBreed(Long farmId, Date eventAt) {
        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        month.getReportDto().setParityStockList(doctorKpiDao.getMonthlyParityStock(farmId, monthStart(eventAt), eventAt));
        month.getReportDto().setBreedStockList(doctorKpiDao.getMonthlyBreedStock(farmId, monthStart(eventAt), eventAt));
        doctorMonthlyReportDao.update(month);
    }

    //更新npd psy
    public void updateMonthlyNpdPsy(Long farmId, Date eventAt) {
        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        month.getReportDto().setNpd(doctorKpiDao.npd(farmId, monthStart(eventAt), eventAt));
        month.getReportDto().setPsy(doctorKpiDao.psy(farmId, monthStart(eventAt), eventAt));
        doctorMonthlyReportDao.update(month);
    }

    //更新断奶7天配种率
    public void updateMonthlyWean7Mate(Long farmId, Date eventAt) {
        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        month.getReportDto().setMateInSeven(doctorKpiDao.getMateInSeven(farmId, monthStart(eventAt), eventAt));
        doctorMonthlyReportDao.update(month);
    }

    //更新公猪生产成绩
    public void updateMonthlyBoarScore(Long farmId, Date eventAt) {
        DoctorMonthlyReport month = getMonthlyReport(farmId, eventAt);
        setBoarScore(month.getReportDto(), farmId, monthStart(eventAt), eventAt);
        doctorMonthlyReportDao.update(month);
    }

    //更新4个月的率统计数据
    public void updateMonthly4MonthRate(Long farmId, Date eventAt) {
        DateUtil.getBeforeMonthEnds(eventAt, 4).forEach(date -> {
            DoctorMonthlyReport month = getMonthlyReport(farmId, date);
            set4MonthRate(month.getReportDto(), farmId, monthStart(date), monthEnd(date));
            doctorMonthlyReportDao.update(month);
        });
    }

    //更新配种情况
    public void updateMonthlyMate(Long farmId, Date eventAt) {

    }

    //更新妊检情况
    public void updateMonthlyPregCheck(Long farmId, Date eventAt) {

    }

    //更新分娩情况
    public void updateMonthlyFarrow(Long farmId, Date eventAt) {

    }

    //更新断奶情况
    public void updateMonthlyWean(Long farmId, Date eventAt) {

    }

    //死亡销售
    public DoctorCommonReportDto setSaleDead(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setSaleSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));                  //母猪
        dto.setSaleBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));                //公猪
        dto.setSaleNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));          //保育猪（产房+保育）
        dto.setSaleFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));            //育肥猪

        dto.setDeadSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));                  //母猪
        dto.setDeadBoar(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));                //公猪
        dto.setDeadFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));            //产房仔猪
        dto.setDeadNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));          //保育猪
        dto.setDeadFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));            //育肥猪
        dto.setDeadHoubei(doctorKpiDao.getDeadHoubei(farmId, startAt, endAt));            //后备猪
        dto.setDeadFarrowRate(doctorKpiDao.getDeadFarrowRate(farmId, startAt, endAt));    //产房死淘率
        dto.setDeadNurseryRate(doctorKpiDao.getDeadNurseryRate(farmId, startAt, endAt));  //保育死淘率
        dto.setDeadFattenRate(doctorKpiDao.getDeadFattenRate(farmId, startAt, endAt));    //育肥死淘率
        return dto;
    }

    //公猪生产成绩
    public DoctorCommonReportDto setBoarScore(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setBoarMateCount(doctorKpiDao.getBoarMateCount(farmId, startAt, endAt));                       //配种次数
        dto.setBoarFirstMateCount(doctorKpiDao.getBoarSowFirstMateCount(farmId, startAt, endAt));          //首次配种母猪数
        dto.setBoarSowPregCount(doctorKpiDao.getBoarSowPregCount(farmId, startAt, endAt));                 //受胎头数
        dto.setBoarSowFarrowCount(doctorKpiDao.getBoarSowFarrowCount(farmId, startAt, endAt));             //产仔母猪数
        dto.setBoarFarrowAvgCount(doctorKpiDao.getBoarSowFarrowAvgCount(farmId, startAt, endAt));          //平均产仔数
        dto.setBoarFarrowLiveAvgCount(doctorKpiDao.getBoarSowFarrowLiveAvgCount(farmId, startAt, endAt));  //平均产活仔数
        dto.setBoarPregRate(doctorKpiDao.getBoarSowPregRate(farmId, startAt, endAt));                      //受胎率
        dto.setBoarFarrowRate(doctorKpiDao.getBoarSowFarrowRate(farmId, startAt, endAt));                  //分娩率
        return dto;
    }

    //配种情况
    public DoctorCommonReportDto setMate(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setMateHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));                   //配后备
        dto.setMateWean(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));                      //配断奶
        dto.setMateFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));                   //配返情
        dto.setMateAbort(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));                 //配流产
        dto.setMateNegtive(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));                    //配阴性
        dto.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));       //估算受胎率
        dto.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        dto.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));  //估算配种分娩率
        dto.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际配种分娩率
        return dto;
    }

    //妊娠检查情况
    public DoctorCommonReportDto setPregCheck(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setCheckPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));                  //妊娠检查阳性
        dto.setCheckFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));                   //返情
        dto.setCheckAbort(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));                 //流产
        dto.setCheckNegtive(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));                   //妊娠检查阴性
        dto.setNpd(doctorKpiDao.npd(farmId, startAt, endAt));                                        //非生产天数
        dto.setPsy(doctorKpiDao.psy(farmId, startAt, endAt));                                        //psy
        dto.setMateInSeven(doctorKpiDao.getMateInSeven(farmId, startAt, endAt));                     //断奶7天配种率
        return dto;
    }

    //分娩情况
    public DoctorCommonReportDto setFarrow(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setFarrowEstimateParity(doctorKpiDao.getPreDelivery(farmId, startAt, endAt));        //预产胎数
        dto.setFarrowNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));                     //分娩窝数
        dto.setFarrowAlive(doctorKpiDao.getDeliveryLive(farmId, startAt, endAt));                //产活仔数
        dto.setFarrowHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));             //产键仔数
        dto.setFarrowWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));                 //产弱仔数
        dto.setFarrowDead(doctorKpiDao.getDeliveryDead(farmId, startAt, endAt));                 //产死仔数
        dto.setFarrowJx(doctorKpiDao.getDeliveryJx(farmId, startAt, endAt));                     //产畸形数
        dto.setFarrowMny(doctorKpiDao.getDeliveryMny(farmId, startAt, endAt));                   //木乃伊数
        dto.setFarrowBlack(doctorKpiDao.getDeliveryBlack(farmId, startAt, endAt));               //产黑胎数
        dto.setFarrowAll(doctorKpiDao.getDeliveryAll(farmId, startAt, endAt));                   //总产仔数
        dto.setFarrowAvgHealth(doctorKpiDao.getDeliveryHealthAvg(farmId, startAt, endAt));       //窝均健仔数
        dto.setFarrowAvgAll(doctorKpiDao.getDeliveryAllAvg(farmId, startAt, endAt));             //窝均产仔数
        dto.setFarrowAvgAlive(doctorKpiDao.getDeliveryLiveAvg(farmId, startAt, endAt));          //窝均活仔数
        dto.setFarrowAvgWeak(doctorKpiDao.getDeliveryWeakAvg(farmId, startAt, endAt));           //窝均弱仔数
        dto.setFarrowAvgWeight(doctorKpiDao.getFarrowWeightAvg(farmId, startAt, endAt));         //分娩活仔均重(kg)
        return dto;
    }

    //断奶情况
    public DoctorCommonReportDto setWean(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setWeanSow(doctorKpiDao.getWeanSow(farmId, startAt, endAt));                         //断奶母猪数
        dto.setWeanPiglet(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));                   //断奶仔猪数
        dto.setWeanAvgWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));       //断奶均重
        dto.setWeanAvgCount(doctorKpiDao.getWeanPigletCountsAvg(farmId, startAt, endAt));        //窝均断奶数
        dto.setWeanAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));             //断奶均日龄
        return dto;
    }

    //4个月的各种率
    public DoctorCommonReportDto set4MonthRate(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));       //估算受胎率
        dto.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        dto.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));  //估算配种分娩率
        dto.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际配种分娩率
        return dto;
    }
}
