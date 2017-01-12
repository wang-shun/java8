package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorKpiDao;
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

    //月报统计结果
    private DoctorCommonReportDto getCommonReportDto(Long farmId, Date startAt, Date endAt) {
        log.info("get monthly report, farmId:{}, startAr:{}, endAt:{}", farmId, startAt, endAt);
        DoctorCommonReportDto dto = new DoctorCommonReportDto();

        //配种情况
        doctorCommonReportManager.setMate(dto, farmId, startAt, endAt);

        //妊娠检查情况
        doctorCommonReportManager.setPregCheck(dto, farmId, startAt, endAt);

        //分娩情况
        doctorCommonReportManager.setFarrow(dto, farmId, startAt, endAt);

        //断奶情况
        doctorCommonReportManager.setWean(dto, farmId, startAt, endAt);

        //销售死淘情况
        doctorCommonReportManager.setSaleDead(dto, farmId, startAt, endAt);

        //公猪生产成绩
        doctorCommonReportManager.setBoarScore(dto, farmId, startAt, endAt);

        //存栏变动月报
        dto.setLiveStockChange(doctorCommonReportManager.getLiveStockChangeReport(farmId, startAt, endAt));

        //存栏结构月报
        dto.setParityStockList(doctorKpiDao.getMonthlyParityStock(farmId, startAt, endAt));
        dto.setBreedStockList(doctorKpiDao.getMonthlyBreedStock(farmId, startAt, endAt));
        return dto;
    }
}
