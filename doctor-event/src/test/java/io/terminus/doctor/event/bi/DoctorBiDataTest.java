package io.terminus.doctor.event.bi;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataSynchronize;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
public class DoctorBiDataTest extends BaseServiceTest {
    @Autowired
    private DoctorReportBiDataSynchronize synchronizer;
    @Autowired
    private DoctorGroupDailyDao doctorGroupDailyDao;

    @Test
    public void fullSynchronizeTest() {
        synchronizer.synchronizeFullBiData();
    }

    @Test
    public void synchronizeRealTimeBiDataTest() {
        synchronizer.synchronizeRealTimeBiData();
    }

    @Test
    public void findByDateTypeTest() {
        System.out.println(doctorGroupDailyDao.findByDateType(DateUtil.toDate("2017-01-01"), DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
    }
}
