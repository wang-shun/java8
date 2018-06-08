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
        try {
            return Response.ok(query.findBoarReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("boar report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("boar.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportDeliver>> deliverReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findDeliverReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("deliver report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("deliver.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportEfficiency>> efficiencyReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findEfficiencyReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("efficiency report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("efficiency.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportFatten>> fattenReport(DoctorDimensionCriteria dimensionCriteria) {

        try {
            return Response.ok(query.findFattenReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("fatten report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("fatten.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportMaterial>> materialReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findMaterialReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("material report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("material.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportMating>> matingReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findMatingReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("mating report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("mating.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportNursery>> nurseryReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findNurseryReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("nursery report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("nursery.report.failed");
        }
    }

    @Override
    public Response<List<DoctorReportReserve>> reserveReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(query.findReserveReportBy(dimensionCriteria));
        } catch (Exception e) {
            log.error("reserve report failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("reserve.report.failed");
        }
    }
}
