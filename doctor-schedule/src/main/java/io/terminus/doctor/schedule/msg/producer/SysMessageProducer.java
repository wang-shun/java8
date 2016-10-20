package io.terminus.doctor.schedule.msg.producer;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Desc: 一般系统消息
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/2
 */
@Component
@Slf4j
public class SysMessageProducer extends AbstractJobProducer {


    @Autowired
    public SysMessageProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                              DoctorMessageRuleReadService doctorMessageRuleReadService,
                              DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                              DoctorMessageReadService doctorMessageReadService,
                              DoctorMessageWriteService doctorMessageWriteService,
                              DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                              DoctorPigReadService doctorPigReadService,
                              DoctorPigWriteService doctorPigWriteService,
                              DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        super(doctorMessageTemplateReadService,
                doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                doctorPigReadService,
                doctorPigWriteService,
                doctorUserDataPermissionReadService,
                Category.SYSTEM);
    }

    @Override
    protected void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("系统消息产生 --- SysMessageProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        // 1. 获取消息模板和规则
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
        Rule rule = template.getRule();

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 查看是否符合规则
            for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
                RuleValue ruleValue = rule.getValues().get(i);
                // 如果是值对比
                if (1 == ruleValue.getRuleType() || 2 == ruleValue.getRuleType()) {
                    if (!checkRuleValue(
                            ruleValue,
                            (double) (DateTime.now().minus(template.getCreatedAt().getTime()).getMillis() / 3600000)
                    )) {
                        return;
                    }
                }
                // 如果是日期对比
                else if (3 == ruleValue.getRuleType() || 4 == ruleValue.getRuleType()) {
                    if (!checkRuleValue(ruleValue, new Date())) {
                        return;
                    }
                }
            }
                    createMessage(subUsers, ruleRole, template.getContent(), null, null, null);
        }
        log.info("系统消息产生 --- SysMessageProducer 结束执行执行, 产生 {} 条消息", messages.size());
    }
}
