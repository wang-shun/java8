package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.common.DoctorLiveStockChangeCommonReport;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.manager.DoctorCommonReportManager;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.model.DoctorWeeklyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 猪场报表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorCommonReportWriteServiceImpl implements DoctorCommonReportWriteService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorCommonReportManager doctorCommonReportManager;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorCommonReportWriteServiceImpl(DoctorCommonReportManager doctorCommonReportManager,
                                              DoctorKpiDao doctorKpiDao) {
        this.doctorCommonReportManager = doctorCommonReportManager;
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public Response<Boolean> createMonthlyReports(List<Long> farmIds, Date sumAt) {
        try {
            Date startAt = new DateTime(sumAt).withDayOfMonth(1).withTimeAtStartOfDay().toDate(); //月初: 2016-08-01 00:00:00
            Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
            List<DoctorMonthlyReport> reports = farmIds.stream()
                    .map(farmId -> getMonthlyReport(farmId, startAt, endAt, sumAt))
                    .collect(Collectors.toList());
            doctorCommonReportManager.createMonthlyReports(reports, Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create monthly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> createMonthlyReport(Long farmId, Date sumAt) {
        try {
            Date startAt = new DateTime(sumAt).withDayOfMonth(1).withTimeAtStartOfDay().toDate(); //月初: 2016-08-01 00:00:00
            Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
            doctorCommonReportManager.createMonthlyReport(farmId, getMonthlyReport(farmId, startAt, endAt, sumAt), Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create monthly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> createWeeklyReports(List<Long> farmIds, Date sumAt) {
        try {
            Date startAt = new DateTime(sumAt).withDayOfWeek(1).withTimeAtStartOfDay().toDate(); //本周周一: 2016-08-01 00:00:00
            Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
            List<DoctorWeeklyReport> reports = farmIds.stream()
                    .map(farmId -> getWeeklyReport(farmId, startAt, endAt, sumAt))
                    .collect(Collectors.toList());
            doctorCommonReportManager.createWeeklyReports(reports, Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create weekly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("weeklyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> createWeeklyReport(Long farmId, Date sumAt) {
        try {
            Date startAt = new DateTime(sumAt).withDayOfWeek(1).withTimeAtStartOfDay().toDate();  //本周周一: 2016-08-01 00:00:00
            Date endAt = new DateTime(Dates.endOfDay(sumAt)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
            doctorCommonReportManager.createWeeklyReport(farmId, getWeeklyReport(farmId, startAt, endAt, sumAt), Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create weekly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("weeklyReport.create.fail");
        }
    }

    @Override
    public Response<DoctorMonthlyReport> initMonthlyReportByFarmIdAndDate(Long farmId, Date date) {
        try {
            Date startAt = new DateTime(date).withDayOfMonth(1).withTimeAtStartOfDay().toDate(); //月初: 2016-08-01 00:00:00
            Date endAt = new DateTime(Dates.endOfDay(date)).plusSeconds(-1).toDate();            //天末: 2016-08-12 23:59:59
            Date sumAt = Dates.startOfDay(date);                                                 //统计日期: 当天天初
            return Response.ok(getMonthlyReport(farmId, startAt, endAt, sumAt));
        } catch (Exception e) {
            log.error("init monthly report by farmId and date failed, farmId:{}, date:{}, cause:{}",
                    farmId, date, Throwables.getStackTraceAsString(e));
            return Response.fail("init.monthly.report.fail");
        }
    }

    //月报
    private DoctorWeeklyReport getWeeklyReport(Long farmId, Date startAt, Date endAt, Date sumAt) {
        DoctorWeeklyReport report = new DoctorWeeklyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);
        report.setData(JSON_MAPPER.toJson(getCommonReportDto(farmId, startAt, endAt)));
        return report;
    }

    //月报
    private DoctorMonthlyReport getMonthlyReport(Long farmId, Date startAt, Date endAt, Date sumAt) {
        DoctorMonthlyReport report = new DoctorMonthlyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);
        report.setData(JSON_MAPPER.toJson(getCommonReportDto(farmId, startAt, endAt)));
        return report;
    }

    //月报统计结果
    private DoctorCommonReportDto getCommonReportDto(Long farmId, Date startAt, Date endAt) {

        log.info("get monthly report, farmId:{}, startAr:{}, endAt:{}", farmId, startAt, endAt);

        DoctorCommonReportDto dto = new DoctorCommonReportDto();
        //配种情况
        dto.setMateHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));                   //配后备
        dto.setMateWean(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));                      //配断奶
        dto.setMateFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));                   //配返情
        dto.setMateAbort(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));                 //配流产
        dto.setMateNegtive(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));                    //配阴性
        dto.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));       //估算受胎率
        dto.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        dto.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, DateUtil.getMonthEnd(new DateTime(endAt)).toDate()));  //估算配种分娩率
        dto.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际配种分娩率

        //妊娠检查情况
        dto.setCheckPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));                  //妊娠检查阳性
        dto.setCheckFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));                   //返情
        dto.setCheckAbort(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));                 //流产
        dto.setCheckNegtive(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));                   //妊娠检查阴性
        dto.setNpd(doctorKpiDao.npd(farmId, startAt, endAt));                                        //非生产天数
        dto.setPsy(doctorKpiDao.psy(farmId, startAt, endAt));                                        //psy
        dto.setMateInSeven(doctorKpiDao.getMateInSeven(farmId, startAt, endAt));                     //断奶7天配种率

        //分娩情况
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

        //断奶情况
        dto.setWeanSow(doctorKpiDao.getWeanSow(farmId, startAt, endAt));                         //断奶母猪数
        dto.setWeanPiglet(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));                   //断奶仔猪数
        dto.setWeanAvgWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));       //断奶均重
        dto.setWeanAvgCount(doctorKpiDao.getWeanPigletCountsAvg(farmId, startAt, endAt));        //窝均断奶数
        dto.setWeanAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));             //断奶均日龄

        //销售情况
        dto.setSaleSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));                  //母猪
        dto.setSaleBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));                //公猪
        dto.setSaleNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));          //保育猪（产房+保育）
        dto.setSaleFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));            //育肥猪

        //死淘情况
        dto.setDeadSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));                  //母猪
        dto.setDeadBoar(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));                //公猪
        dto.setDeadFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));            //产房仔猪
        dto.setDeadNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));          //保育猪
        dto.setDeadFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));            //育肥猪
        dto.setDeadHoubei(doctorKpiDao.getDeadHoubei(farmId, startAt, endAt));            //后备猪
        dto.setDeadFarrowRate(doctorKpiDao.getDeadFarrowRate(farmId, startAt, endAt));    //产房死淘率
        dto.setDeadNurseryRate(doctorKpiDao.getDeadNurseryRate(farmId, startAt, endAt));  //保育死淘率
        dto.setDeadFattenRate(doctorKpiDao.getDeadFattenRate(farmId, startAt, endAt));    //育肥死淘率

        //公猪生产成绩
        dto.setBoarMateCount(doctorKpiDao.getBoarMateCount(farmId, startAt, endAt));                       //配种次数
        dto.setBoarFirstMateCount(doctorKpiDao.getBoarSowFirstMateCount(farmId, startAt, endAt));          //首次配种母猪数
        dto.setBoarSowPregCount(doctorKpiDao.getBoarSowPregCount(farmId, startAt, endAt));                 //受胎头数
        dto.setBoarSowFarrowCount(doctorKpiDao.getBoarSowFarrowCount(farmId, startAt, endAt));             //产仔母猪数
        dto.setBoarFarrowAvgCount(doctorKpiDao.getBoarSowFarrowAvgCount(farmId, startAt, endAt));          //平均产仔数
        dto.setBoarFarrowLiveAvgCount(doctorKpiDao.getBoarSowFarrowLiveAvgCount(farmId, startAt, endAt));  //平均产活仔数
        dto.setBoarPregRate(doctorKpiDao.getBoarSowPregRate(farmId, startAt, endAt));                      //受胎率
        dto.setBoarFarrowRate(doctorKpiDao.getBoarSowFarrowRate(farmId, startAt, endAt));                  //分娩率

        //存栏变动月报
        dto.setLiveStockChange(getLiveStockChangeReport(farmId, startAt, endAt));

        //存栏结构月报
        dto.setParityStockList(doctorKpiDao.getMonthlyParityStock(farmId, startAt, endAt));
        dto.setBreedStockList(doctorKpiDao.getMonthlyBreedStock(farmId, startAt, endAt));
        return dto;
    }

    //存栏变动月报
    private DoctorLiveStockChangeCommonReport getLiveStockChangeReport(Long farmId, Date startAt, Date endAt) {
        DoctorLiveStockChangeCommonReport report = new DoctorLiveStockChangeCommonReport();

        DoctorLiveStockChangeCommonReport begin = doctorKpiDao.getMonthlyLiveStockChangeBegin(farmId, startAt);
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
}
