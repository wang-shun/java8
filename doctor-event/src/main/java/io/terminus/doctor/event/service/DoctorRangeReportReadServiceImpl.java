package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorRangeReportDao;
import io.terminus.doctor.event.model.DoctorRangeReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 指标月报读服务实现类
 * Date: 2017-04-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorRangeReportReadServiceImpl implements DoctorRangeReportReadService {

    private final DoctorRangeReportDao doctorRangeReportDao;

    @Autowired
    public DoctorRangeReportReadServiceImpl(DoctorRangeReportDao doctorRangeReportDao) {
        this.doctorRangeReportDao = doctorRangeReportDao;
    }

    @Override
    public Response<DoctorRangeReport> findDoctorRangeReportById(Long doctorRangeReportId) {
        try {
            return Response.ok(doctorRangeReportDao.findById(doctorRangeReportId));
        } catch (Exception e) {
            log.error("find doctorRangeReport by id failed, doctorRangeReportId:{}, cause:{}", doctorRangeReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorRangeReport.find.fail");
        }
    }
}
