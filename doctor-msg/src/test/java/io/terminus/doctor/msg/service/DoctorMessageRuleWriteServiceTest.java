package io.terminus.doctor.msg.service;

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
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
public class DoctorMessageRuleWriteServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    @Autowired
    private DoctorMessageRuleReadService doctorMessageRuleReadService;
    
    @Autowired
    private DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;
    
    @Autowired
    private DoctorMessageRuleTemplateWriteService doctorMessageRuleTemplateWriteService;

    /**
     * 当farm审核通过后的与消息模板绑定的初始化
     * @see DoctorMessageRuleWriteService#initTemplate(Long) 
     */
    @Test
    public void test_INIT_templateAndFarm() {
        doctorMessageRuleWriteService.initTemplate(1L);
        List<DoctorMessageRule> rules = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByFarmId(1L));
        System.out.println(rules.size());
    }

    /**
     * 更新模板信息
     * @see DoctorMessageRuleWriteService#updateMessageRule(DoctorMessageRule)
     */
    @Test
    public void test_UPDATE_Template() {
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(1L));
        if (template != null) {
            template = BeanMapper.map(template, DoctorMessageRuleTemplate.class);
            Rule rule = template.getRule();
            rule.setChannels("0,1,2");
            template.setRuleValue(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(rule));
            // 更新(记录历史)
            doctorMessageRuleTemplateWriteService.updateMessageRuleTemplate(template);
        }
        template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(1L));
        System.out.println(template);
    }
}
