package io.terminus.doctor.event.service;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.msg.Rule;
import io.terminus.doctor.event.model.DoctorMessageRule;
import io.terminus.doctor.event.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.event.test.BaseServiceTest;
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

    @Autowired
    private DoctorMessageTemplateReadService doctorMessageTemplateReadService;

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
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(2L));
        if (template != null) {
            template = BeanMapper.map(template, DoctorMessageRuleTemplate.class);
            Rule rule = template.getRule();
            rule.setChannels("0,1,2");
            template.setRuleValue(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(rule));
            // 更新(记录历史)
            doctorMessageRuleTemplateWriteService.updateMessageRuleTemplate(template);
        }
        template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(2L));
        System.out.println(template);
        // 查看绑定的默认rule是否修改了
        List<DoctorMessageRule> rules = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByTplId(2L));
        System.out.println(rules);
    }

    /**
     * 获取parana模板编译后的内容
     */
    @Test
    public void test_QUERY_MessageTemplate() {
        String code = RespHelper.orServEx(doctorMessageTemplateReadService.getMessageContentWithCache("user.register.code", ImmutableMap.of("code", "888888")));
        System.out.println(code);
    }
}
