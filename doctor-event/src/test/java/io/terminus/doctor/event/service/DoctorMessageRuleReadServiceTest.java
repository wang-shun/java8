package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMessageRule;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiao on 16/7/28.
 */
public class DoctorMessageRuleReadServiceTest extends BaseServiceTest {
    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    @Autowired
    private DoctorMessageRuleReadService doctorMessageRuleReadService;

    /**
     *测试 根据farmId And templateName 查询
     */
    @Test
    public void test_QUERY_FindMessageRulesByFarmIdAndTemplateName() {
        Response<List<DoctorMessageRule>> response= doctorMessageRuleReadService.findMessageRulesByFarmIdAndTemplateName(12350l,"待配种");
        System.out.println(response.getResult());
    }
}
