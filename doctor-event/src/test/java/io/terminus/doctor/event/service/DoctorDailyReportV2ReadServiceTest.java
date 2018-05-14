package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
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

    @Test
    public void findSowReportTest() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(1L);
        doctorDimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        doctorDimensionCriteria.setStartAt(DateUtil.toDate("2018-01-01"));
        doctorDimensionCriteria.setEndAt(DateUtil.toDate("2018-02-01"));
        doctorDimensionCriteria.setDateType(DateDimension.DAY.getValue());
        Response<List<DoctorReportSow>> doctorReportSowResponse = doctorDailyReportV2ReadService.sowReport(doctorDimensionCriteria);
        Assert.assertTrue(doctorReportSowResponse.isSuccess());
        System.out.println(doctorReportSowResponse.getResult().size());
    }
}
