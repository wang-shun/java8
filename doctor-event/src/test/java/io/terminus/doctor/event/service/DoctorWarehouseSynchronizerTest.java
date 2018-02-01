package io.terminus.doctor.event.service;

import io.terminus.doctor.event.reportBi.synchronizer.DoctorWarehouseSynchronizer;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/1/19.
 */
public class DoctorWarehouseSynchronizerTest extends BaseServiceTest {

    @Autowired
    private DoctorWarehouseSynchronizer warehouseSynchronizer;

    @Test
    public void t() {
        warehouseSynchronizer.sync(new Date());
    }

}
