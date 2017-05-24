package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 日报写服务实现类
 * Date: 2017-04-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportWriteServiceImpl implements DoctorDailyReportWriteService {

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyReportManager doctorDailyReportManager;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportDao doctorDailyReportDao,
                                             DoctorDailyReportManager doctorDailyReportManager) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportManager = doctorDailyReportManager;
    }

    @Override
    public Response<Long> createDoctorDailyReport(DoctorDailyReport doctorDailyReport) {
        try {
            doctorDailyReportDao.create(doctorDailyReport);
            return Response.ok(doctorDailyReport.getId());
        } catch (Exception e) {
            log.error("create doctorDailyReport failed, doctorDailyReport:{}, cause:{}", doctorDailyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDoctorDailyReport(DoctorDailyReport doctorDailyReport) {
        try {
            return Response.ok(doctorDailyReportDao.update(doctorDailyReport));
        } catch (Exception e) {
            log.error("update doctorDailyReport failed, doctorDailyReport:{}, cause:{}", doctorDailyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyReport.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDoctorDailyReportById(Long doctorDailyReportId) {
        try {
            return Response.ok(doctorDailyReportDao.delete(doctorDailyReportId));
        } catch (Exception e) {
            log.error("delete doctorDailyReport failed, doctorDailyReportId:{}, cause:{}", doctorDailyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorDailyReport.delete.fail");
        }
    }

    @Override
    public Response<Boolean> generateYesterdayAndTodayReports(List<Long> farmIds) {
        Date today = Dates.startOfDay(new Date());
        Date yesterday = new DateTime(today).minusDays(1).toDate();
        farmIds.forEach(farmId -> {
            log.info("create daily report start, farmId: {}, date: {}, now: {}", farmId, DateUtil.toDateString(today), DateUtil.toDateTimeString(new Date()));
            doctorDailyReportManager.createOrUpdateReport(farmId, yesterday);
            doctorDailyReportManager.createOrUpdateReport(farmId, today);
        });
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> createDailyReports(Long farmId, Date beginDate, Date endDate) {
        try{
            beginDate = Dates.startOfDay(beginDate);
            endDate = Dates.startOfDay(endDate);
            while(!beginDate.after(endDate)){
                log.info("create daily report start, farmId: {}, date: {}, now: {}", farmId, DateUtil.toDateString(beginDate), DateUtil.toDateTimeString(new Date()));
                doctorDailyReportManager.createOrUpdateReport(farmId, beginDate);
                beginDate = new DateTime(beginDate).plusDays(1).toDate();
            }
            return Response.ok(Boolean.TRUE);
        }catch(Exception e) {
            log.error("updateHistoryDailyReport failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.history.daily.report.fail");
        }
    }

    @Override
    public Response<Boolean> createDailyReports(List<Long> farmIds, Date date) {
        farmIds.forEach(farmId -> {
            log.info("create daily report start, farmId: {}, date: {}, now: {}", farmId, DateUtil.toDateString(date), DateUtil.toDateTimeString(new Date()));
            doctorDailyReportManager.createOrUpdateReport(farmId, date);
        });
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> createDailyReports(Long farmId, Date date) {
        try{
            log.info("create daily report start, farmId: {}, date: {}, now: {}", farmId, DateUtil.toDateString(date), DateUtil.toDateTimeString(new Date()));
            doctorDailyReportManager.createOrUpdateReport(farmId, date);
        }catch(Exception e) {
            log.error("create daily report failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.history.daily.report.fail");
        }
        return Response.ok(Boolean.TRUE);
    }

}
