package io.terminus.doctor.msg.service;

import com.google.common.collect.ImmutableList;
import io.terminus.doctor.msg.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/12
 */
public class DoctorMessageRuleRoleWriteServiceImplTest extends BaseServiceTest {

    @Autowired
    private DoctorMessageRuleRoleWriteService doctorMessageRuleRoleWriteService;

    @Test
    @Rollback(false)
    public void relateRuleRolesByRuleId() throws Exception {
        doctorMessageRuleRoleWriteService.relateRuleRolesByRuleId(6L, ImmutableList.of(1L, 2L));
    }

    @Test
    @Rollback(false)
    public void relateRuleRolesByRoleId() throws Exception {
        doctorMessageRuleRoleWriteService.relateRuleRolesByRoleId(1L, ImmutableList.of(7L));
    }
}