package io.terminus.doctor.msg.service;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.BaseServiceTest;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiao on 16/7/28.
 */
public class DoctorMessageRuleReadServiceTest extends BaseServiceTest{
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
