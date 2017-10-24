package io.terminus.doctor.event.concurrent;

import io.terminus.doctor.event.dao.DoctorDailyReportDao;
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

    @Test
    public void dailyReportConcurrentTest() {
        DoctorDailyReport dailyReport = doctorDailyReportDao.findById(1);
        for (int i = 0; i < 2; i++) {
            new Thread(()-> manager.createOrUpdateDailyPig(dailyReport)).start();
        }
    }
}
