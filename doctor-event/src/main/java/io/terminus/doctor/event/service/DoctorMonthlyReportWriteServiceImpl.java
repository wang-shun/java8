package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportDto;
import io.terminus.doctor.event.manager.DoctorMonthlyReportManager;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 猪场月报表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMonthlyReportWriteServiceImpl implements DoctorMonthlyReportWriteService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorMonthlyReportManager doctorMonthlyReportManager;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorMonthlyReportWriteServiceImpl(DoctorMonthlyReportManager doctorMonthlyReportManager,
                                               DoctorKpiDao doctorKpiDao) {
        this.doctorMonthlyReportManager = doctorMonthlyReportManager;
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
            doctorMonthlyReportManager.createMonthlyReports(reports, Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create monthly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.create.fail");
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
    private DoctorMonthlyReport getMonthlyReport(Long farmId, Date startAt, Date endAt, Date sumAt) {
        DoctorMonthlyReport report = new DoctorMonthlyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);
        report.setData(JSON_MAPPER.toJson(getMonthlyReportDto(farmId, startAt, endAt)));
        return report;
    }

    //月报统计结果
    private DoctorMonthlyReportDto getMonthlyReportDto(Long farmId, Date startAt, Date endAt) {
        DoctorMonthlyReportDto dto = new DoctorMonthlyReportDto();
        //配种情况
        dto.setMateHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));                   //配后备
        dto.setMateWean(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));                      //配断奶
        dto.setMateFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));                   //配返情
        dto.setMateAbort(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));                 //配流产
        dto.setMateNegtive(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));                    //配阴性
        dto.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, endAt));       //估算受胎率
        dto.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        dto.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, endAt));  //估算配种分娩率
        dto.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际配种分娩率

        //妊娠检查情况
        dto.setCheckPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));                  //妊娠检查阳性
        dto.setCheckFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));                   //返情
        dto.setCheckAbort(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));                 //流产
        dto.setCheckNegtive(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));                   //妊娠检查阴性
        dto.setNpd(doctorKpiDao.npd(farmId, startAt, endAt));                                        //非生产天数
        dto.setPsy(doctorKpiDao.psy(farmId, startAt, endAt));                                        //psy

        //分娩情况
        dto.setFarrowEstimateParity(doctorKpiDao.getPreDelivery(farmId, startAt, endAt));        //预产胎数
        dto.setFarrowNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));                     //分娩窝数
        dto.setFarrowAlive(doctorKpiDao.getDeliveryLive(farmId, startAt, endAt));                //产活仔数
        dto.setFarrowHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));             //产键仔数
        dto.setFarrowWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));                 //产弱仔数
        dto.setFarrowDead(doctorKpiDao.getDeliveryDead(farmId, startAt, endAt));                 //产死仔数
        dto.setFarrowMny(doctorKpiDao.getDeliveryMny(farmId, startAt, endAt));                   //木乃伊数
        dto.setFarrowAll(doctorKpiDao.getDeliveryAll(farmId, startAt, endAt));                   //总产仔数
        dto.setFarrowAvgHealth(doctorKpiDao.getDeliveryHealthAvg(farmId, startAt, endAt));       //窝均健仔数
        dto.setFarrowAvgAll(doctorKpiDao.getDeliveryAllAvg(farmId, startAt, endAt));             //窝均产仔数
        dto.setFarrowAvgAlive(doctorKpiDao.getDeliveryLiveAvg(farmId, startAt, endAt));          //窝均活仔数

        //断奶情况
        dto.setWeanSow(doctorKpiDao.getWeanSow(farmId, startAt, endAt));                         //断奶母猪数
        dto.setWeanPiglet(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));                   //断奶仔猪数
        dto.setWeanAvgWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));       //断奶均重
        dto.setWeanAvgCount(doctorKpiDao.getWeanPigletCountsAvg(farmId, startAt, endAt));        //窝均断奶数

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
        dto.setDeadFarrowRate(doctorKpiDao.getDeadFarrowRate(farmId, startAt, endAt));    //产房死淘率
        dto.setDeadNurseryRate(doctorKpiDao.getDeadNurseryRate(farmId, startAt, endAt));  //保育死淘率
        dto.setDeadFattenRate(doctorKpiDao.getDeadFattenRate(farmId, startAt, endAt));    //育肥死淘率
        return dto;
    }
}
