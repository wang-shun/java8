package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    private final DoctorDailyReportManager doctorDailyReportManager;

    @Autowired
    public DoctorDailyReportWriteServiceImpl(DoctorDailyReportManager doctorDailyReportManager) {
        this.doctorDailyReportManager = doctorDailyReportManager;
    }

    @Override
    public Response<Boolean> createDailyReports(List<DoctorDailyReportDto> dailyReports, Date sumAt) {
        try {
            doctorDailyReportManager.createDailyReports(dailyReports, Dates.startOfDay(sumAt));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create dailyReport failed, dailyReports:{}, cause:{}", dailyReports, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.create.fail");
        }
    }
}
