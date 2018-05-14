package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xjn
 * @date 18/5/14
 * email xiaojiannan@terminus.io
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportV2ReadServiceImpl implements DoctorDailyReportV2ReadService {

    private final DoctorReportBiDataQuery query;

    @Autowired
    public DoctorDailyReportV2ReadServiceImpl(DoctorReportBiDataQuery query) {
        this.query = query;
    }

    @Override
    public Response<List<DoctorReportSow>> sowReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findSowReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("sow report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("sow.report.failed");
        }
    }
}
