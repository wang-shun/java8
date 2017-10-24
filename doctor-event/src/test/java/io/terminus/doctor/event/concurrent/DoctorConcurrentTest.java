package io.terminus.doctor.event.concurrent;

import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.helper.DoctorConcurrentControl;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/10/24.
 */
public class DoctorConcurrentTest extends BaseServiceTest {
    @Autowired
    private DoctorDailyReportManager manager;
    @Autowired
    private DoctorDailyReportDao doctorDailyReportDao;
    @Autowired
    private DoctorConcurrentControl doctorConcurrentControl;

    @Test
    public void dailyReportConcurrentTest() throws InterruptedException {
        DoctorDailyReport dailyReport = doctorDailyReportDao.findById(1);
        for (int i = 0; i < 2; i++) {
            new Thread(()-> manager.createOrUpdateDailyPig(dailyReport)).start();
        }
        Thread.sleep(2000);
    }

    @Test
    public void setNxTest() throws InterruptedException {
        doctorConcurrentControl.delKey("1");
        System.out.println(doctorConcurrentControl.setKey("1"));
        Thread.sleep(6000);
        System.out.println(doctorConcurrentControl.setKey("1"));
    }

}
