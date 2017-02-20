package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorWeeklyReportDao;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.common.DoctorLiveStockChangeCommonReport;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.model.DoctorWeeklyReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Slf4j
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
     * 删除sumAt数据, 再创建
     *
     * @param farmId 猪场id
     * @param sumAt  统计日期
     */
    @Transactional
    public DoctorMonthlyReport createMonthlyReport(Long farmId, Date sumAt) {
        Date startAt = new DateTime(sumAt).withDayOfMonth(1).withTimeAtStartOfDay().toDate(); //月初: 2016-08-01 00:00:00
        Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
        sumAt = Dates.startOfDay(sumAt);

        DoctorMonthlyReport monthlyReport = getMonthlyReport(farmId, startAt, endAt, sumAt);
        doctorMonthlyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorMonthlyReportDao.create(monthlyReport);
        return monthlyReport;
    }

    /**
     * 删除sumAt数据, 再创建
     *
     * @param farmId 猪场id
     * @param sumAt  统计日期
     */
    @Transactional
    public DoctorWeeklyReport createWeeklyReport(Long farmId, Date sumAt) {
        Date startAt = new DateTime(sumAt).withDayOfWeek(1).withTimeAtStartOfDay().toDate();  //本周周一: 2016-08-01 00:00:00
        Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
        sumAt = Dates.startOfDay(sumAt);

        DoctorWeeklyReport weeklyReport = getWeeklyReport(farmId, startAt, endAt, sumAt);
        doctorWeeklyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorWeeklyReportDao.create(weeklyReport);
        return weeklyReport;
    }

    //周报
    private DoctorWeeklyReport getWeeklyReport(Long farmId, Date startAt, Date endAt, Date sumAt) {
        DoctorWeeklyReport report = new DoctorWeeklyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);
        report.setReportDto(getCommonReportDto(farmId, startAt, endAt));
        return report;
    }

    //月报
    private DoctorMonthlyReport getMonthlyReport(Long farmId, Date startAt, Date endAt, Date sumAt) {
        DoctorMonthlyReport report = new DoctorMonthlyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);
        report.setReportDto(getCommonReportDto(farmId, startAt, endAt));
        return report;
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

    //更新存栏变动
    public Boolean updateLiveStockChange(FarmIdAndEventAt fe) {
        try {
            log.info("updateLiveStockChange fe:{}", fe);
            DoctorLiveStockChangeCommonReport monthLiveStock = getLiveStockChangeReport(fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            month.getReportDto().setLiveStockChange(monthLiveStock);
            doctorMonthlyReportDao.update(month);

            DoctorLiveStockChangeCommonReport weekLiveStock = getLiveStockChangeReport(fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            week.getReportDto().setLiveStockChange(weekLiveStock);
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonLiveStockChange failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新销售死淘
    public Boolean updateSaleDead(FarmIdAndEventAt fe) {
        try {
            log.info("updateSaleDead fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setSaleDead(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setSaleDead(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonSaleDead failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新胎次分布，品类分布
    public Boolean updateParityBreed(FarmIdAndEventAt fe) {
        try {
            log.info("updateParityBreed fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            month.getReportDto().setParityStockList(doctorKpiDao.getMonthlyParityStock(fe.getFarmId(), fe.monthStart(), fe.monthEnd()));
            month.getReportDto().setBreedStockList(doctorKpiDao.getMonthlyBreedStock(fe.getFarmId(), fe.monthStart(), fe.monthEnd()));
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            week.getReportDto().setParityStockList(doctorKpiDao.getMonthlyParityStock(fe.getFarmId(), fe.weekStart(), fe.weekEnd()));
            week.getReportDto().setBreedStockList(doctorKpiDao.getMonthlyBreedStock(fe.getFarmId(), fe.weekStart(), fe.weekEnd()));
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonParityBreed failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新npd psy
    public Boolean updateNpdPsy(FarmIdAndEventAt fe) {
        try {
            log.info("updateNpdPsy fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            month.getReportDto().setNpd(doctorKpiDao.npd(fe.getFarmId(), fe.monthStart(), fe.monthEnd()));
            month.getReportDto().setPsy(doctorKpiDao.psy(fe.getFarmId(), fe.monthStart(), fe.monthEnd()));
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            week.getReportDto().setNpd(doctorKpiDao.npd(fe.getFarmId(), fe.weekStart(), fe.weekEnd()));
            week.getReportDto().setPsy(doctorKpiDao.psy(fe.getFarmId(), fe.weekStart(), fe.weekEnd()));
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonNpdPsy failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新断奶7天配种率
    public Boolean updateWean7Mate(FarmIdAndEventAt fe) {
        try {
            log.info("updateWean7Mate fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            month.getReportDto().setMateInSeven(doctorKpiDao.getMateInSeven(fe.getFarmId(), fe.monthStart(), fe.monthEnd()));
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            week.getReportDto().setMateInSeven(doctorKpiDao.getMateInSeven(fe.getFarmId(), fe.weekStart(), fe.weekEnd()));
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonWean7Mate failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新公猪生产成绩
    public Boolean updateBoarScore(FarmIdAndEventAt fe) {
        try {
            log.info("updateBoarScore fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setBoarScore(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setBoarScore(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonBoarScore failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新4个月的率统计数据
    public Boolean update4MonthRate(FarmIdAndEventAt fe) {
        try {
            log.info("update4MonthRate fe:{}", fe);
            DateUtil.getBeforeMonthEnds(fe.getEventAt(), 4).forEach(date -> {
                DoctorMonthlyReport month = findMonthlyReportIfNullInit(new FarmIdAndEventAt(fe.getFarmId(), date));
                set4MonthRate(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
                doctorMonthlyReportDao.update(month);
            });

            DateUtil.getBeforeWeekEnds(fe.getEventAt(), 4).forEach(date -> {
                DoctorWeeklyReport week = findWeeklyReportIfNullInit(new FarmIdAndEventAt(fe.getFarmId(), date));
                set4MonthRate(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
                doctorWeeklyReportDao.update(week);
            });
        } catch (Exception e) {
            log.error("updateCommon4MonthRate failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新配种情况
    public Boolean updateMate(FarmIdAndEventAt fe) {
        try {
            log.info("updateMate fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setMate(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setMate(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonMate failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新妊检情况
    public Boolean updatePregCheck(FarmIdAndEventAt fe) {
        try {
            log.info("updatePregCheck fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setPregCheck(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setPregCheck(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonPregCheck failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新分娩情况
    public Boolean updateFarrow(FarmIdAndEventAt fe) {
        try {
            log.info("updateFarrow fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setFarrow(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setFarrow(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonFarrow failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //更新断奶情况
    public Boolean updateWean(FarmIdAndEventAt fe) {
        try {
            log.info("updateWean fe:{}", fe);
            DoctorMonthlyReport month = findMonthlyReportIfNullInit(fe);
            setWean(month.getReportDto(), fe.getFarmId(), fe.monthStart(), fe.monthEnd());
            doctorMonthlyReportDao.update(month);

            DoctorWeeklyReport week = findWeeklyReportIfNullInit(fe);
            setWean(week.getReportDto(), fe.getFarmId(), fe.weekStart(), fe.weekEnd());
            doctorWeeklyReportDao.update(week);
        } catch (Exception e) {
            log.error("updateCommonWean failed, farmIdAndEventAt:{}, cause:{}", fe, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    //死亡销售
    private DoctorCommonReportDto setSaleDead(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setSaleSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));                  //母猪
        dto.setSaleBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));                //公猪
        dto.setSaleNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));          //保育猪（产房+保育）
        dto.setSaleFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));            //育肥猪
        dto.setSaleHoubei(doctorKpiDao.getSaleHoubei(farmId, startAt, endAt));            //后备猪

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
    private DoctorCommonReportDto setBoarScore(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
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
    private DoctorCommonReportDto setMate(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
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
    private DoctorCommonReportDto setPregCheck(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
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
    private DoctorCommonReportDto setFarrow(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
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
    private DoctorCommonReportDto setWean(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setWeanSow(doctorKpiDao.getWeanSow(farmId, startAt, endAt));                         //断奶母猪数
        dto.setWeanPiglet(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));                   //断奶仔猪数
        dto.setWeanAvgWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));       //断奶均重
        dto.setWeanAvgCount(doctorKpiDao.getWeanPigletCountsAvg(farmId, startAt, endAt));        //窝均断奶数
        dto.setWeanAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));             //断奶均日龄
        return dto;
    }

    //4个月的各种率
    private DoctorCommonReportDto set4MonthRate(DoctorCommonReportDto dto, Long farmId, Date startAt, Date endAt) {
        dto.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));       //估算受胎率
        dto.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        dto.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));  //估算配种分娩率
        dto.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际配种分娩率
        return dto;
    }

    //月报存月末
    private DoctorMonthlyReport findMonthlyReportIfNullInit(FarmIdAndEventAt fe) {
        DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(fe.getFarmId(), fe.monthEnd());
        if (report == null) {
            report = createMonthlyReport(fe.getFarmId(), fe.getEventAt());
        }
        return report;
    }

    //周报是周一
    private DoctorWeeklyReport findWeeklyReportIfNullInit(FarmIdAndEventAt fe) {
        DoctorWeeklyReport report = doctorWeeklyReportDao.findByFarmIdAndSumAt(fe.getFarmId(), fe.weekEnd());
        if (report == null) {
            report = createWeeklyReport(fe.getFarmId(), fe.getEventAt());
        }
        return report;
    }

    //月报统计结果
    private DoctorCommonReportDto getCommonReportDto(Long farmId, Date startAt, Date endAt) {
        log.info("get monthly report, farmId:{}, startAr:{}, endAt:{}", farmId, startAt, endAt);
        DoctorCommonReportDto dto = new DoctorCommonReportDto();

        //配种情况
        setMate(dto, farmId, startAt, endAt);

        //妊娠检查情况
        setPregCheck(dto, farmId, startAt, endAt);

        //分娩情况
        setFarrow(dto, farmId, startAt, endAt);

        //断奶情况
        setWean(dto, farmId, startAt, endAt);

        //销售死淘情况
        setSaleDead(dto, farmId, startAt, endAt);

        //公猪生产成绩
        setBoarScore(dto, farmId, startAt, endAt);

        //存栏变动月报
        dto.setLiveStockChange(getLiveStockChangeReport(farmId, startAt, endAt));

        //存栏结构月报
        dto.setParityStockList(doctorKpiDao.getMonthlyParityStock(farmId, startAt, endAt));
        dto.setBreedStockList(doctorKpiDao.getMonthlyBreedStock(farmId, startAt, endAt));
        return dto;
    }


    /**
     * 猪场id和eventAt类，用于传递查询月报需要的参数
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FarmIdAndEventAt {
        private Long farmId;
        private Date eventAt;

        Date monthStart() {
            return DateUtil.monthStart(eventAt);
        }

        Date monthEnd() {
            return DateUtil.monthEnd(eventAt);
        }

        Date weekStart() {
            return DateUtil.weekStart(eventAt);
        }

        Date weekEnd() {
            return DateUtil.weekEnd(eventAt);
        }
    }
}
