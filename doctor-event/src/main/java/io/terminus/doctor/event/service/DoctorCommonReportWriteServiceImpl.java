package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.manager.DoctorCommonReportManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    @Autowired
    public DoctorCommonReportWriteServiceImpl(DoctorCommonReportManager doctorCommonReportManager) {
        this.doctorCommonReportManager = doctorCommonReportManager;
    }

    @Override
    public Response<Boolean> createMonthlyReport(Long farmId, Date sumAt) {
        try {
            doctorCommonReportManager.createMonthlyReport(farmId, sumAt);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create monthly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> createWeeklyReport(Long farmId, Date sumAt) {
        try {
            doctorCommonReportManager.createWeeklyReport(farmId, sumAt);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create weekly reports failed, sumAt:{}, cause:{}", sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("weeklyReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> update4MonthReports(Long farmId, Date date) {
        try {
            doctorCommonReportManager.update4MonthRate(new DoctorCommonReportManager.FarmIdAndEventAt(farmId, date));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update 4month. report rate failed, farmId:{}, date:{}, cause:{}", farmId, date, Throwables.getStackTraceAsString(e));
            return Response.fail("update.4month.report.rate.fail");
        }
    }
}
