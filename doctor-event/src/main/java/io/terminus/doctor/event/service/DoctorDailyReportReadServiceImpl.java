package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪场日报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportReadServiceImpl implements DoctorDailyReportReadService {

    private final DoctorDailyReportDao doctorDailyReportDao;

    @Autowired
    public DoctorDailyReportReadServiceImpl(DoctorDailyReportDao doctorDailyReportDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
    }

    @Override
    public Response<DoctorDailyReport> findDailyReportById(Long dailyReportId) {
        try {
            return Response.ok(doctorDailyReportDao.findById(dailyReportId));
        } catch (Exception e) {
            log.error("find dailyReport by id failed, dailyReportId:{}, cause:{}", dailyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.find.fail");
        }
    }

    @Override
    public Response<DoctorDailyReport> findDailyReportByFarmIdAndSumAt(Long farmId, String suAt) {
        try {
            return null;
        } catch (Exception e) {
            log.error("find dailyReport by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.find.fail");
        }
    }
}
