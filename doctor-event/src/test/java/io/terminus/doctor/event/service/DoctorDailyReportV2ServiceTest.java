package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
public class DoctorDailyReportV2ServiceTest extends BaseServiceTest {
    @Autowired
    private DoctorDailyReportV2Service doctorDailyReportV2Service;

    @Test
    public void flushGroupDaily() {
        Response<Boolean> response = doctorDailyReportV2Service.flushGroupDaily(1L,"2017-01-01", "2017-01-02");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());
    }

    @Test
    public void flushGroupDailyFroType() {
        Response<Boolean> response = doctorDailyReportV2Service.flushGroupDaily(1L, 2,
                "2017-01-01", "2017-01-02");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());

    }

    @Test
    public void flushpigDaily() {
        Response<Boolean> response = doctorDailyReportV2Service.flushPigDaily(2L,"2017-01-01", "2017-01-05");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());
    }
}
