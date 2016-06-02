package io.terminus.doctor.msg.producer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.doctor.msg.dao.DoctorMessageDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleRoleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 消息产生者抽象类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
@Slf4j
public abstract class AbstractProducer implements IProducer {

    protected DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao;
    protected DoctorMessageRuleDao doctorMessageRuleDao;
    protected DoctorMessageRuleRoleDao doctorMessageRuleRoleDao;
    protected DoctorMessageDao doctorMessageDao;
    protected Category category;

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
    public void produce(List<SubUser> subUsers) {

        List<DoctorMessageRuleTemplate> ruleTemplates = doctorMessageRuleTemplateDao.findByCategory(category.getKey());
        for (int k = 0; ruleTemplates != null && k < ruleTemplates.size(); k++) {
            DoctorMessageRuleTemplate ruleTemplate = ruleTemplates.get(k);
            // 如果不正常, 则不继续执行
            if(ruleTemplate == null || !Objects.equals(ruleTemplate.getStatus(), DoctorMessageRuleTemplate.Status.NORMAL.getValue())) {
                return;
            }
            // 1. 如果是系统消息
            log.info("消息类型是 : {}, subUsers size is {}", ruleTemplate.getType(), subUsers.size());
            if (Objects.equals(DoctorMessageRuleTemplate.Type.SYSTEM.getValue(), ruleTemplate.getType())) {
                // 获取最新发送的系统消息(系统消息是对应到模板的)
                DoctorMessage latestMessage = getLatestSysMessage(ruleTemplate.getId());
                // 检查消息是否在频率范围之内
                if (!checkFrequence(latestMessage, ruleTemplate.getRule())) {
                    return;
                }
                DoctorMessageRuleRole ruleRole = DoctorMessageRuleRole.builder()
                        .templateId(ruleTemplate.getId())
                        .ruleValue(ruleTemplate.getRuleValue())
                        .build();
                // 获取信息 (针对所有的角色/user)
                List<DoctorMessage> message = message(ruleRole, subUsers);
                if(message != null && message.size() > 0) {
                    log.info("生成消息对象: {}", message);
                    doctorMessageDao.creates(message);
                }
            }

            // 2. 如果是预警或警报消息
            else{
                List<DoctorMessageRuleRole> ruleRoles = doctorMessageRuleRoleDao.findByTplId(ruleTemplate.getId());
                for (int i = 0; ruleRoles != null && i < ruleRoles.size(); i++) {
                    DoctorMessageRuleRole ruleRole = ruleRoles.get(i);
                    // 查询对应的message_rule
                    DoctorMessageRule messageRule = doctorMessageRuleDao.findById(ruleRole.getRuleId());
                    if(messageRule == null || !Objects.equals(messageRule.getStatus(), DoctorMessageRule.Status.NORMAL.getValue())) {
                        continue;
                    }
                    // 获取最新的发送消息
                    DoctorMessage latestMessage = getLatestWarnMessage(ruleRole.getTemplateId(), ruleRole.getFarmId(), ruleRole.getRoleId());
                    // 检查消息是否在频率范围之内
                    if (!checkFrequence(latestMessage, ruleRole.getRule())) {
                        return;
                    }
                    // 获取信息
                    List<DoctorMessage> message = message(ruleRole, subUsers);
                    if(message != null && message.size() > 0) {
                        doctorMessageDao.creates(message);
                    }
                }
            }
        }
    }

    /**
     * 根据规则与角色绑定, 获取发送的消息
     * @param ruleRole
     * @return
     */
    protected abstract List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers);

    /**
     * 检查消息符合规则RuleValue
     * @param ruleValue     规则
     * @param value         比较值
     * @return
     */
    protected boolean checkRuleValue (RuleValue ruleValue, Double value) {
        // 1. 值类型
        if (Objects.equals(RuleValue.RuleType.VALUE.getValue(), ruleValue.getRuleType())) {
            return Objects.equals(ruleValue.getValue(), value);
        }
        // 2. 值范围类型
        if (Objects.equals(RuleValue.RuleType.VALUE_RANGE.getValue(), ruleValue.getRuleType())) {
            if (ruleValue.getLeftValue() != null && value < ruleValue.getLeftValue()) {
                return false;
            }
            if (ruleValue.getRightValue() != null && value > ruleValue.getRightValue()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 检查消息符合规则RuleValue
     * @param ruleValue     规则
     * @param date          比较日期
     * @return
     */
    protected boolean checkRuleValue (RuleValue ruleValue, Date date) {
        // 1. 日期类型
        if (Objects.equals(RuleValue.RuleType.DATE.getValue(), ruleValue.getRuleType())) {
            return new DateTime(ruleValue.getDate()).minus(date.getTime()).getMillis() == 0;
        }
        // 2. 日期范围类型
        if (Objects.equals(RuleValue.RuleType.DATE_RANGE.getValue(), ruleValue.getRuleType())) {
            if (ruleValue.getLeftDate() != null && new DateTime(date).isBefore(new DateTime(ruleValue.getLeftDate()))) {
                return false;
            }
            if (ruleValue.getRightDate() != null && new DateTime(date).isAfter(new DateTime(ruleValue.getRightDate()))) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 创建DoctorMessage对象
     * @param subUsers  所有子账号
     * @param ruleRole  规则角色
     * @param channel   发送渠道
     * @param jsonData  填充数据
     * @return
     */
    protected List<DoctorMessage> createMessage(List<SubUser> subUsers, DoctorMessageRuleRole ruleRole, Integer channel, String jsonData) {
        List<DoctorMessage> messages = Lists.newArrayList();
        DoctorMessageRuleTemplate template = doctorMessageRuleTemplateDao.findById(ruleRole.getTemplateId());
        if (subUsers != null && subUsers.size() > 0) {
            // 主账户
            subUsers.stream().map(SubUser::getParentUserId).collect(Collectors.toSet())
                    .forEach(parentId -> messages.add(
                            DoctorMessage.builder()
                                    .farmId(ruleRole.getFarmId())
                                    .ruleId(ruleRole.getRuleId())
                                    .roleId(ruleRole.getRoleId())
                                    .userId(parentId)
                                    .templateId(ruleRole.getTemplateId())
                                    .type(template.getType())
                                    .category(template.getCategory())
                                    .data(jsonData)
                                    .channel(channel)
                                    .url(ruleRole.getRule().getUrl())
                                    .status(DoctorMessage.Status.NORMAL.getValue())
                                    .createdBy(template.getUpdatedBy())
                                    .build()
                    ));

            // 1. 如果是系统消息
            if (ruleRole.getRoleId() == null) {
                // 子账户
                subUsers.stream().forEach(subUser -> messages.add(
                        DoctorMessage.builder()
                                .farmId(ruleRole.getFarmId())
                                .ruleId(ruleRole.getRuleId())
                                .roleId(ruleRole.getRoleId())
                                .userId(subUser.getUserId())
                                .templateId(ruleRole.getTemplateId())
                                .type(template.getType())
                                .category(template.getCategory())
                                .data(jsonData)
                                .channel(channel)
                                .url(ruleRole.getRule().getUrl())
                                .status(DoctorMessage.Status.NORMAL.getValue())
                                .createdBy(template.getUpdatedBy())
                                .build()
                        ));
            }
            // 2. 否则预警或警报消息
            else {
                // 子账户
                subUsers.stream().filter(sub -> Objects.equals(sub.getRoleId(), ruleRole.getRoleId()))
                        .forEach(subUser -> messages.add(
                                DoctorMessage.builder()
                                        .farmId(ruleRole.getFarmId())
                                        .ruleId(ruleRole.getRuleId())
                                        .roleId(ruleRole.getRoleId())
                                        .userId(subUser.getUserId())
                                        .templateId(ruleRole.getTemplateId())
                                        .type(template.getType())
                                        .category(template.getCategory())
                                        .data(jsonData)
                                        .channel(channel)
                                        .url(ruleRole.getRule().getUrl())
                                        .status(DoctorMessage.Status.NORMAL.getValue())
                                        .createdBy(template.getUpdatedBy())
                                        .build()
                ));
            }
        }
        return messages;
    }

    /**
     * 获取最新发送的预警或警报的消息
     * @param templateId    规则模板id
     * @param farmId        猪场id
     * @param roleId        角色id
     * @return
     */
    private DoctorMessage getLatestWarnMessage(Long templateId, Long farmId, Long roleId) {
        List<DoctorMessage> messages = doctorMessageDao.list(ImmutableMap.of("farmId", farmId, "templateId", templateId, "roleId", roleId));
        if (messages != null && messages.size() > 0) {
            return messages.get(0);
        }
        return null;
    }

    /**
     * 获取最新发送的系统消息(系统消息是对应到模板的)
     * @param templateId
     * @return
     */
    private DoctorMessage getLatestSysMessage(Long templateId) {
        List<DoctorMessage> messages = doctorMessageDao.list(ImmutableMap.of("templateId", templateId));
        if (messages != null && messages.size() > 0) {
            return messages.get(0);
        }
        return null;
    }

    /**
     * 检查消息是否在频率范围之内
     * @param message   消息
     * @param rule      规则
     * @return
     */
    private boolean checkFrequence(DoctorMessage message, Rule rule) {
        if (message != null) {
            // 查看在频率范围内是否已经发送
            if (rule != null && rule.getFrequence() != null) {
                // 小于0表示只发送一次
                if (rule.getFrequence() < 0) {
                    return false;
                }
                // 未到频率时间
                if (new DateTime(message.getCreatedAt())
                        .isAfter(DateTime.now().minusHours(rule.getFrequence()))) {
                    return false;
                }
            }
        }
        return true;
    }
}
