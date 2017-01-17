package io.terminus.doctor.event.service;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xjn on 16/12/23.
 * 月报测试
 */
public class DoctorMonthLyReportServiceTest extends BaseServiceTest{
    @Autowired
    private DoctorCommonReportReadService doctorCommonReportReadService;

    @Autowired
    private DoctorDailyReportReadService doctorDailyReportReadService;

    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Test
    public void test_monthlyReport(){
        List<DoctorMonthlyReport> doctorMonthlyReports = RespHelper.orServEx(doctorCommonReportReadService.findMonthlyReports("2016-11-08"));
        System.out.println(doctorMonthlyReports);
    }

    @Test
    public void test_getEveryGroupInfo() {
        System.out.println(RespHelper.orServEx(doctorCommonReportReadService.findEveryGroupInfo("2016-11-08")));
        System.out.println(doctorKpiDao.getEveryGroupInfo("2016-11-08"));
    }

    @Test
    public void test_dailyReport(){
        System.out.println(RespHelper.orServEx(doctorDailyReportReadService.findDailyReportBySumAt(DateUtil.toDate("2016-11-08"))));
    }
}
