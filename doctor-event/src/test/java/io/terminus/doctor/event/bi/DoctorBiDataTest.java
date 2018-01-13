package io.terminus.doctor.event.bi;

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

    @Test
    public void fullSynchronizerTest() {
        synchronizer.synchronizeFullBiData();
    }
}
