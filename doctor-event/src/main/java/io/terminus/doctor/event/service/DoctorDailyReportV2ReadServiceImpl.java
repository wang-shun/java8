package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
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

    @Override
    public Response<List<DoctorReportBoar>> boarReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportDeliver>> deliverReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportEfficiency>> efficiencyReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportFatten>> fattenReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportMaterial>> materialReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportMating>> matingReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportNursery>> nurseryReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @Override
    public Response<List<DoctorReportReserve>> reserveReport(DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }
}
