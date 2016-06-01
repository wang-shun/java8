package io.terminus.doctor.msg.service;

import io.terminus.doctor.msg.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
public class DoctorMessageRuleWriteServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    @Test
    public void test() {
        doctorMessageRuleWriteService.initTemplate(1L);
    }

}
