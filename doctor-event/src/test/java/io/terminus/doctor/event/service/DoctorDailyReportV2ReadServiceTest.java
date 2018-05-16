package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author xjn
 * @date 18/5/14
 * email xiaojiannan@terminus.io
 */
public class DoctorDailyReportV2ReadServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorDailyReportV2ReadService doctorDailyReportV2ReadService;
    private  DoctorDimensionCriteria doctorDimensionCriteria;

    @Before
    public void criteria() {
        doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(1L);
        doctorDimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        doctorDimensionCriteria.setStartAt(DateUtil.toDate("2018-01-01"));
        doctorDimensionCriteria.setEndAt(DateUtil.toDate("2018-02-01"));
        doctorDimensionCriteria.setDateType(DateDimension.DAY.getValue());
        doctorDimensionCriteria.setIsNecessaryTotal(IsOrNot.YES.getKey());
    }

    @Test
    public void findSowReportTest() {
        Response<List<DoctorReportSow>> doctorReportSowResponse = doctorDailyReportV2ReadService.sowReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findBoarReportTest() {
        Response<List<DoctorReportBoar>> doctorReportSowResponse = doctorDailyReportV2ReadService.boarReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findDeliverReportTest() {
        Response<List<DoctorReportDeliver>> doctorReportSowResponse = doctorDailyReportV2ReadService.deliverReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findMatingReportTest() {
        Response<List<DoctorReportMating>> doctorReportSowResponse = doctorDailyReportV2ReadService.matingReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findFattenReportTest() {
        Response<List<DoctorReportFatten>> doctorReportSowResponse = doctorDailyReportV2ReadService.fattenReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findNurseryReportTest() {
        Response<List<DoctorReportNursery>> doctorReportSowResponse = doctorDailyReportV2ReadService.nurseryReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findReserveReportTest() {
        Response<List<DoctorReportReserve>> doctorReportSowResponse = doctorDailyReportV2ReadService.reserveReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findMaterialReportTest() {
        Response<List<DoctorReportReserve>> doctorReportSowResponse = doctorDailyReportV2ReadService.reserveReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }

    @Test
    public void findEfficiencyReportTest() {
        Response<List<DoctorReportEfficiency>> doctorReportSowResponse = doctorDailyReportV2ReadService.efficiencyReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }
}
