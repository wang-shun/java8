package io.terminus.doctor.msg.service;

import io.terminus.doctor.msg.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
public class DoctorMessageRuleWriteServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    /**
     * 当farm审核通过后的与消息模板绑定的初始化
     */
    @Test
    @Rollback(false)
    public void test_INIT_templateAndFarm() {
        doctorMessageRuleWriteService.initTemplate(1L);
    }

}
