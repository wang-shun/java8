package io.terminus.doctor.msg.service;

import io.terminus.doctor.msg.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Created by xiao on 16/10/12.
 */
public class DoctorMessageUserReadServiceTest extends BaseServiceTest {
    @Autowired
    private DoctorMessageUserReadService doctorMessageUserReadService;

    @Autowired
    private DoctorMessageUserWriteService doctorMessageUserWriteService;

    @Test
    @Rollback(value = false)
    public void create(){

    }
}
