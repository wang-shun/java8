package io.terminus.doctor.msg.producer;

import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.msg.dao.DoctorMessageDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleRoleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
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
public class SysMessageProducer extends AbstractProducer {


    @Autowired
    public SysMessageProducer(DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao,
                               DoctorMessageRuleDao doctorMessageRuleDao,
                               DoctorMessageRuleRoleDao doctorMessageRuleRoleDao,
                               DoctorMessageDao doctorMessageDao) {
        super(doctorMessageRuleTemplateDao,
                doctorMessageRuleDao,
                doctorMessageRuleRoleDao,
                doctorMessageDao,
                Category.SYSTEM);
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {

        List<DoctorMessage> messages = Lists.newArrayList();

        // 1. 获取消息模板和规则
        DoctorMessageRuleTemplate template = doctorMessageRuleTemplateDao.findById(ruleRole.getTemplateId());
        Rule rule = template.getRule();

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 查看是否符合规则
            for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
                RuleValue ruleValue = rule.getValues().get(i);
                // 如果是值对比
                if (1 == ruleValue.getRuleType() || 2 == ruleValue.getRuleType()) {
                    if (!checkRuleValue(
                            ruleValue,
                            (double) (DateTime.now().minus(template.getUpdatedAt().getTime()).getMillis() / 3600000)
                    )) {
                        return messages;
                    }
                }
                // 如果是日期对比
                else if (3 == ruleValue.getRuleType() || 4 == ruleValue.getRuleType()) {
                    if (!checkRuleValue(ruleValue, new Date())) {
                        return messages;
                    }
                }
            }
            // 根据不同渠道存储消息
            Splitters.COMMA.splitToList(rule.getChannels()).forEach(channel ->
                    messages.addAll(createMessage(subUsers, ruleRole, Integer.parseInt(channel), template.getContent())));
        }
        return messages;
    }
}
