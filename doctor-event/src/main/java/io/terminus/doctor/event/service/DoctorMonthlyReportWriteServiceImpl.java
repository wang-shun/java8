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
        dto.setMateHoubei(0);                //配后备
        dto.setMateWean(0);                  //配断奶
        dto.setMateFanqing(0);               //配返情
        dto.setMateAbort(0);                 //配流产
        dto.setMateNegtive(0);               //配阴性
        dto.setMateEstimatePregRate(0);      //估算受胎率
        dto.setMateRealPregRate(0);          //实际受胎率
        dto.setMateEstimateFarrowingRate(0); //估算配种分娩率
        dto.setMateRealFarrowingRate(0);     //实际配种分娩率
        dto.setCheckPositive(0);             //妊娠检查阳性
        dto.setCheckFanqing(0);              //返情
        dto.setCheckAbort(0);                //流产
        dto.setCheckNegtive(0);              //妊娠检查阴性
        dto.setFarrowEstimateParity(0);      //预产胎数
        dto.setFarrowNest(0);                //分娩窝数
        dto.setFarrowAlive(0);               //产活仔数
        dto.setFarrowHealth(0);              //产键仔数
        dto.setFarrowWeak(0);                //产弱仔数
        dto.setFarrowDead(0);                //产死仔数
        dto.setFarrowMny(0);                 //木乃伊数
        dto.setFarrowAll(0);                 //总产仔数
        dto.setFarrowAvgHealth(0);           //窝均健仔数
        dto.setFarrowAvgAll(0);              //窝均产仔数
        dto.setFarrowAvgAlive(0);            //窝均活仔数
        dto.setWeanSow(0);                   //断奶母猪数
        dto.setWeanPiglet(0);                //断奶仔猪数
        dto.setWeanAvgWeight(0);             //断奶均重
        dto.setWeanAvgCount(0);              //窝均断奶数
        dto.setSaleSow(0);                   //母猪
        dto.setSaleBoar(0);                  //公猪
        dto.setSaleNursery(0);               //保育猪（产房+保育）
        dto.setSaleFatten(0);                //育肥猪
        dto.setDeadSow(0);                   //母猪
        dto.setDeadBoar(0);                  //公猪
        dto.setDeadFarrow(0);                //产房仔猪
        dto.setDeadNursery(0);               //保育猪
        dto.setDeadFatten(0);                //育肥猪
        dto.setDeadFarrowRate(0);            //产房死淘率
        dto.setDeadNurseryRate(0);           //保育死淘率
        dto.setDeadFattenRate(0);            //育肥死淘率
        dto.setNpd(0);                       //非生产天数
        dto.setPsy(0);                       //psy
        return dto;
    }
}
