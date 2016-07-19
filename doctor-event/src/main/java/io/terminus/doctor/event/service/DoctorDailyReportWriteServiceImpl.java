package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪场日报表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportWriteServiceImpl implements DoctorDailyReportWriteService {

    private final DoctorDailyReportDao doctorDailyReportDao;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportDao doctorDailyReportDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
    }

    @Override
    public Response<Long> createDailyReport(DoctorDailyReport dailyReport) {
        try {
            doctorDailyReportDao.create(dailyReport);
            return Response.ok(dailyReport.getId());
        } catch (Exception e) {
            log.error("create dailyReport failed, dailyReport:{}, cause:{}", dailyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDailyReport(DoctorDailyReport dailyReport) {
        try {
            return Response.ok(doctorDailyReportDao.update(dailyReport));
        } catch (Exception e) {
            log.error("update dailyReport failed, dailyReport:{}, cause:{}", dailyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDailyReportById(Long dailyReportId) {
        try {
            return Response.ok(doctorDailyReportDao.delete(dailyReportId));
        } catch (Exception e) {
            log.error("delete dailyReport failed, dailyReportId:{}, cause:{}", dailyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.delete.fail");
        }
    }
}
