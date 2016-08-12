package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;

    @Autowired
    public DoctorMonthlyReportWriteServiceImpl(DoctorMonthlyReportDao doctorMonthlyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
    }

    @Override
    public Response<Long> createMonthlyReport(DoctorMonthlyReport monthlyReport) {
        try {
            doctorMonthlyReportDao.create(monthlyReport);
            return Response.ok(monthlyReport.getId());
        } catch (Exception e) {
            log.error("create monthlyReport failed, monthlyReport:{}, cause:{}", monthlyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateMonthlyReport(DoctorMonthlyReport monthlyReport) {
        try {
            return Response.ok(doctorMonthlyReportDao.update(monthlyReport));
        } catch (Exception e) {
            log.error("update monthlyReport failed, monthlyReport:{}, cause:{}", monthlyReport, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteMonthlyReportById(Long monthlyReportId) {
        try {
            return Response.ok(doctorMonthlyReportDao.delete(monthlyReportId));
        } catch (Exception e) {
            log.error("delete monthlyReport failed, monthlyReportId:{}, cause:{}", monthlyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.delete.fail");
        }
    }
}
