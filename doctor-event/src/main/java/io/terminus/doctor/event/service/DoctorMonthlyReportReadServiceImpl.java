package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪场月报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMonthlyReportReadServiceImpl implements DoctorMonthlyReportReadService {

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;

    @Autowired
    public DoctorMonthlyReportReadServiceImpl(DoctorMonthlyReportDao doctorMonthlyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
    }

    @Override
    public Response<DoctorMonthlyReport> findMonthlyReportById(Long monthlyReportId) {
        try {
            return Response.ok(doctorMonthlyReportDao.findById(monthlyReportId));
        } catch (Exception e) {
            log.error("find monthlyReport by id failed, monthlyReportId:{}, cause:{}", monthlyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMonthlyReport>> findMonthlyReportsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorMonthlyReportDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find monthlyReport by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("monthlyReport.find.fail");
        }
    }
}
