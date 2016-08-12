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
        dto.setMateHoubei(0);
        dto.setMateWean(0);
        dto.setMateFanqing(0);
        dto.setMateAbort(0);
        dto.setMateNegtive(0);
        dto.setMateEstimatePregRate(0);
        dto.setMateRealPregRate(0);
        dto.setMateEstimateFarrowingRate(0);
        dto.setMateRealFarrowingRate(0);
        dto.setCheckPositive(0);
        dto.setCheckFanqing(0);
        dto.setCheckAbort(0);
        dto.setCheckNegtive(0);
        dto.setFarrowEstimateParity(0);
        dto.setFarrowNest(0);
        dto.setFarrowAlive(0);
        dto.setFarrowHealth(0);
        dto.setFarrowWeak(0);
        dto.setFarrowDead(0);
        dto.setFarrowMny(0);
        dto.setFarrowAll(0);
        dto.setFarrowAvgHealth(0);
        dto.setFarrowAvgAll(0);
        dto.setFarrowAvgAlive(0);
        dto.setWeanSow(0);
        dto.setWeanPiglet(0);
        dto.setWeanAvgWeight(0);
        dto.setWeanAvgCount(0);
        dto.setSaleSow(0);
        dto.setSaleBoar(0);
        dto.setSaleNursery(0);
        dto.setSaleFatten(0);
        dto.setDeadSow(0);
        dto.setDeadBoar(0);
        dto.setDeadFarrow(0);
        dto.setDeadNursery(0);
        dto.setDeadFatten(0);
        dto.setDeadFarrowRate(0);
        dto.setDeadNurseryRate(0);
        dto.setDeadFattenRate(0);
        dto.setNpd(0);
        dto.setPsy(0);
        return dto;
    }
}
