package io.terminus.doctor.msg.producer;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.msg.dao.DoctorMessageDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleRoleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 消息产生者抽象类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
public abstract class AbstractProducer implements IProducer {

    private DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao;
    private DoctorMessageRuleDao doctorMessageRuleDao;
    private DoctorMessageRuleRoleDao doctorMessageRuleRoleDao;
    private DoctorMessageDao doctorMessageDao;
    private Category category;

    public AbstractProducer(DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao,
                            DoctorMessageRuleDao doctorMessageRuleDao,
                            DoctorMessageRuleRoleDao doctorMessageRuleRoleDao,
                            DoctorMessageDao doctorMessageDao,
                            Category category) {
        this.doctorMessageRuleTemplateDao = doctorMessageRuleTemplateDao;
        this.doctorMessageRuleDao = doctorMessageRuleDao;
        this.doctorMessageRuleRoleDao = doctorMessageRuleRoleDao;
        this.doctorMessageDao = doctorMessageDao;
        this.category = category;
    }

    @Override
    public void produce() {
        DoctorMessageRuleTemplate ruleTemplate = doctorMessageRuleTemplateDao.findByCategory(category.getKey());
        // 如果不正常, 则不继续执行
        if(!Objects.equals(ruleTemplate.getStatus(), DoctorMessageRuleTemplate.Status.NORMAL.getValue())) {
            return;
        }
        List<DoctorMessageRuleRole> ruleRoles = doctorMessageRuleRoleDao.findByTplId(ruleTemplate.getId());
        for (int i = 0; ruleRoles != null && i < ruleRoles.size(); i++) {
            DoctorMessageRuleRole ruleRole = ruleRoles.get(i);
            // 查询对应的message_rule
            DoctorMessageRule messageRule = doctorMessageRuleDao.findById(ruleRole.getRuleId());
            if(!Objects.equals(messageRule.getStatus(), DoctorMessageRule.Status.NORMAL.getValue())) {
                continue;
            }

            // 获取最新的发送消息
            DoctorMessage latestMessage = getLatestMessage(ruleRole.getTemplateId(), ruleRole.getFarmId(), ruleRole.getRoleId());
            if (latestMessage != null) {
                // 查看在频率范围内是否已经发送
                Rule rule = ruleRole.getRule();
                if (rule != null) {
                    // 小于0表示只发送一次
                    if (rule.getFrequence() < 0) {
                        return;
                    }
                    // 未到频率时间
                    if (new DateTime(latestMessage.getCreatedAt())
                            .isAfter(DateTime.now().minusHours(rule.getFrequence()))) {
                        return;
                    }
                }
            }

            // 获取信息
            List<DoctorMessage> message = message(ruleRole);
            if(message != null && message.size() > 0) {
                doctorMessageDao.creates(message);
            }
        }
    }

    /**
     * 根据规则与角色绑定, 获取发送的消息
     * @param ruleRole
     * @return
     */
    protected abstract List<DoctorMessage> message(DoctorMessageRuleRole ruleRole);

    /**
     * 获取最新发送的的消息
     * @param templateId    规则模板id
     * @param farmId        猪场id
     * @param roleId        角色id
     * @return
     */
    private DoctorMessage getLatestMessage(Long templateId, Long farmId, Long roleId) {
        List<DoctorMessage> messages = doctorMessageDao.list(ImmutableMap.of("farmId", farmId, "templateId", templateId, "roleId", roleId));
        if (messages != null && messages.size() > 0) {
            return messages.get(0);
        }
        return null;
    }
}
