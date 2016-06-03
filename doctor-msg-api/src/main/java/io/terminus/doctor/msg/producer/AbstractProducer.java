package io.terminus.doctor.msg.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
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

    protected ObjectMapper MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    protected DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;

    protected DoctorMessageRuleReadService doctorMessageRuleReadService;

    protected DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService;

    protected DoctorMessageReadService doctorMessageReadService;

    protected DoctorMessageWriteService doctorMessageWriteService;

    protected Category category;

    public AbstractProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                            DoctorMessageRuleReadService doctorMessageRuleReadService,
                            DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                            DoctorMessageReadService doctorMessageReadService,
                            DoctorMessageWriteService doctorMessageWriteService,
                            Category category) {
        this.doctorMessageRuleTemplateReadService = doctorMessageRuleTemplateReadService;
        this.doctorMessageRuleReadService = doctorMessageRuleReadService;
        this.doctorMessageRuleRoleReadService = doctorMessageRuleRoleReadService;
        this.doctorMessageReadService = doctorMessageReadService;
        this.doctorMessageWriteService = doctorMessageWriteService;
        this.category = category;
    }

    @Override
    public void produce(List<SubUser> subUsers) {

        List<DoctorMessageRuleTemplate> ruleTemplates = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findByCategory(category.getKey()));
        for (int k = 0; ruleTemplates != null && k < ruleTemplates.size(); k++) {
            DoctorMessageRuleTemplate ruleTemplate = ruleTemplates.get(k);
            // 如果不正常, 则不继续执行
            if(ruleTemplate == null || !Objects.equals(ruleTemplate.getStatus(), DoctorMessageRuleTemplate.Status.NORMAL.getValue())) {
                return;
            }
            // 1. 如果是系统消息
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
                    doctorMessageWriteService.createMessages(message);
                }
            }

            // 2. 如果是预警或警报消息
            else{
                List<DoctorMessageRuleRole> ruleRoles = RespHelper.orServEx(doctorMessageRuleRoleReadService.findByTplId(ruleTemplate.getId()));
                for (int i = 0; ruleRoles != null && i < ruleRoles.size(); i++) {
                    DoctorMessageRuleRole ruleRole = ruleRoles.get(i);
                    // 查询对应的message_rule
                    DoctorMessageRule messageRule = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRuleById(ruleRole.getRuleId()));
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
                        doctorMessageWriteService.createMessages(message);
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
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
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
                                    .messageTemplate(getTemplateName(template.getMessageTemplate(), channel))
                                    .type(template.getType())
                                    .category(template.getCategory())
                                    .data(jsonData)
                                    .channel(channel)
                                    .url(getUrl(ruleRole.getRule().getUrl(), channel))
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
                                .messageTemplate(getTemplateName(template.getMessageTemplate(), channel))
                                .type(template.getType())
                                .category(template.getCategory())
                                .data(jsonData)
                                .channel(channel)
                                .url(getUrl(ruleRole.getRule().getUrl(), channel))
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
                                        .messageTemplate(getTemplateName(template.getMessageTemplate(), channel))
                                        .type(template.getType())
                                        .category(template.getCategory())
                                        .data(jsonData)
                                        .channel(channel)
                                        .url(getUrl(ruleRole.getRule().getUrl(), channel))
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
        List<DoctorMessage> messages = RespHelper.orServEx(doctorMessageReadService.findMessageByCriteria(
                ImmutableMap.of("farmId", farmId, "templateId", templateId, "roleId", roleId)));
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
        List<DoctorMessage> messages = RespHelper.orServEx(doctorMessageReadService.findMessageByCriteria(
                ImmutableMap.of("templateId", templateId)));
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

    /**
     * 获取具体的模板名称
     * @param tplName   模板基名
     * @param channel   渠道
     * @return
     */
    private String getTemplateName(String tplName, Integer channel) {
        Rule.Channel type = Rule.Channel.from(channel);
        if (type != null) {
            return tplName + "." + type.getSuffix();
        }
        return tplName;
    }

    /**
     * 获取不同的url
     * @param url       url
     * @param channel   发送渠道, app推送需要带 http:// 的全url
     */
    private String getUrl(String url, Integer channel) {
        // 如果是 app 推送
        if (Objects.equals(channel, Rule.Channel.APPPUSH.getValue())) {
            return url;
        }
        // 否则去除前缀
        if (url.contains("http://")) {
            String url1 = url.substring(7);
            url1 = url1.substring(url1.indexOf("/"));
            return url1;
        }else {
            return url.substring(url.indexOf("/"));
        }
    }

    /**
     * 获取总页数
     * @param total     总数量
     * @param size      批量获取的数量
     * @return
     */
    protected Long getPageSize(Long total, Long size) {
        size = MoreObjects.firstNonNull(size, 100L);
        Long page = 0L;
        if (total != null) {
            if (total % size == 0) {
                page = total / size;
            }else {
                page = total / size + 1;
            }
        }
        return page;
    }
}
