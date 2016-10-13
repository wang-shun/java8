package io.terminus.doctor.msg.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.model.DoctorMessageUser;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageUserWriteService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    protected DoctorMessageTemplateReadService doctorMessageTemplateReadService;

    protected DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;

    protected DoctorMessageRuleReadService doctorMessageRuleReadService;

    protected DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService;

    protected DoctorMessageReadService doctorMessageReadService;

    protected DoctorMessageWriteService doctorMessageWriteService;

    protected Category category;

    @Autowired
    protected DoctorMessageUserWriteService doctorMessageUserWriteService;

    public AbstractProducer(DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                            DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                            DoctorMessageRuleReadService doctorMessageRuleReadService,
                            DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                            DoctorMessageReadService doctorMessageReadService,
                            DoctorMessageWriteService doctorMessageWriteService,
                            Category category) {
        this.doctorMessageTemplateReadService = doctorMessageTemplateReadService;
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
        for (int i = 0; ruleTemplates != null && i < ruleTemplates.size(); i++) {
            DoctorMessageRuleTemplate ruleTemplate = ruleTemplates.get(i);
            // 如果不正常, 则不继续执行
            if (ruleTemplate == null || !Objects.equals(ruleTemplate.getStatus(), DoctorMessageRuleTemplate.Status.NORMAL.getValue())) {
                return;
            }

            // 1. 如果是系统消息
            if (Objects.equals(DoctorMessageRuleTemplate.Type.SYSTEM.getValue(), ruleTemplate.getType())) {
                Stopwatch stopwatch = Stopwatch.createStarted();
                log.info("[AbstractProducer] {} -> 系统消息产生, starting......", ruleTemplate.getName());
                // 获取最新发送的系统消息(系统消息是对应到模板的)
                DoctorMessage latestMessage = getLatestSysMessage(ruleTemplate.getId());
                // 检查消息是否在频率范围之内
                if (!checkFrequence(latestMessage, ruleTemplate.getRule())) {
                    log.info("[AbstractProducer] {} -> 系统消息未在频率范围内, ending......", ruleTemplate.getName());
                    return;
                }
                DoctorMessageRuleRole ruleRole = DoctorMessageRuleRole.builder()
                        .templateId(ruleTemplate.getId())
                        .ruleValue(ruleTemplate.getRuleValue())
                        .build();
                // 获取信息 (针对所有的角色/user)
                message(ruleRole, subUsers);
                stopwatch.stop();
                log.info("[AbstractProducer] {} -> 系统消息产生正常结束, 耗时 {}ms end......", ruleTemplate.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }

            // 2. 如果是预警或警报消息
            else {
                Stopwatch stopWatch = Stopwatch.createStarted();
                log.info("[AbstractProducer] {} -> 预警消息产生, starting......", ruleTemplate.getName());

                // > 记录对应每个用户的消息
                List<DoctorMessageRule> messageRules = RespHelper.orServEx(doctorMessageRuleReadService.findNormalMessageRulesByTplId(ruleTemplate.getId()));

                for (int j = 0; messageRules != null && j < messageRules.size(); j++) {
                    DoctorMessageRule messageRule = messageRules.get(j);
                    if (!Objects.equals(messageRule.getStatus(), DoctorMessageRule.Status.NORMAL.getValue())) {
                        continue;
                    }
                    // 获取最新的发送消息
                    DoctorMessage latestMessage = getLatestWarnMessage(messageRule.getTemplateId(), messageRule.getFarmId());
                    // 检查消息是否在频率范围之内
                    if (!checkFrequence(latestMessage, messageRule.getRule())) {
                        continue;
                    }
                    // 获取信息
                    log.info("[AbstractProducer] {} -> 预警消息产生", ruleTemplate.getName());
                    //将之前消息置为无效
//                    setMessageIsExpired(messageRule);
                    DoctorMessageRuleRole ruleRole = DoctorMessageRuleRole.builder()
                            .ruleId(messageRule.getId())
                            .templateId(messageRule.getTemplateId())
                            .farmId(messageRule.getFarmId())
                            .ruleValue(messageRule.getRuleValue())
                            .build();
                    message(ruleRole,
                            subUsers.stream().filter(sub -> sub.getFarmIds().contains(messageRule.getFarmId())).collect(Collectors.toList()));
//                    if (Arguments.notEmpty(message)) {
//                        //分批次插入数据
//                        Lists.partition(message, 5000).forEach(list -> doctorMessageWriteService.createMessages(list));
//                    }
                }

                // List<DoctorMessageRuleRole> ruleRoles = RespHelper.orServEx(doctorMessageRuleRoleReadService.findByTplId(ruleTemplate.getId()));
                // > 记录对应每个用户的消息
                /*for (int j = 0; ruleRoles != null && j < ruleRoles.size(); j++) {
                    DoctorMessageRuleRole ruleRole = ruleRoles.get(j);
                    // 查询对应的message_rule
                    DoctorMessageRule messageRule = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRuleById(ruleRole.getRuleId()));
                    if(messageRule == null || !Objects.equals(messageRule.getStatus(), DoctorMessageRule.Status.NORMAL.getValue())) {
                        continue;
                    }
                    // 获取最新的发送消息
                    DoctorMessage latestMessage = getLatestWarnMessage(ruleRole.getTemplateId(), ruleRole.getFarmId(), ruleRole.getRoleId());
                    // 检查消息是否在频率范围之内
                    if (!checkFrequence(latestMessage, ruleRole.getRule())) {
                        continue;
                    }
                    // 获取信息
                    log.info("[AbstractProducer] {} -> 预警消息产生, roleId: {}", ruleTemplate.getName(), ruleRole.getRoleId());
                    List<DoctorMessage> message = message(ruleRole,
                            subUsers.stream().filter(sub -> Objects.equals(sub.getRoleId(), ruleRole.getRoleId())).collect(Collectors.toList()));
                    if(message != null && message.size() > 0) {
                        doctorMessageWriteService.createMessages(message);
                    }
                }*/
                stopWatch.stop();
                log.info("[AbstractProducer] {} -> 预警消息产生结束, 耗时 {}ms, ending......", ruleTemplate.getName(), stopWatch.elapsed(TimeUnit.MILLISECONDS));
            }

            // > 记录对应每只猪类型的消息
            ImmutableList<Integer> ofCategories = ImmutableList.of(
                    // 当前支持这些消息提醒
                    Category.SOW_NEEDWEAN.getKey(),
                    Category.SOW_BREEDING.getKey(),
                    Category.SOW_BIRTHDATE.getKey(),
                    Category.SOW_PREGCHECK.getKey(),
                    Category.SOW_BACK_FAT.getKey(),
                    Category.SOW_ELIMINATE.getKey());
            RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByTplId(ruleTemplate.getId())).forEach(doctorMessageRule -> {
                if (ofCategories.contains(doctorMessageRule.getCategory())) {
                    recordPigMessages(doctorMessageRule);
                }
            });
        }
    }

    /**
     * 根据规则与角色绑定, 获取发送的消息
     *
     * @param ruleRole
     * @return
     */
    protected abstract void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers);

    /**
     * 根据规则与猪场的绑定, 获取每只猪的消息
     *
     * @return
     */
    protected abstract void recordPigMessages(DoctorMessageRule messageRule);

    /**
     * 检查用户是否含有数据的权限
     *
     * @return
     */
    protected abstract boolean hasUserAuth(Long userId, Long farmId);

    /**
     * 检查消息符合规则RuleValue
     *
     * @param ruleValue 规则
     * @param value     比较值
     * @return
     */
    protected boolean checkRuleValue(RuleValue ruleValue, Double value) {
        if (ruleValue == null) {
            return true;
        }
        // 1. 值类型
        if (Objects.equals(RuleValue.RuleType.VALUE.getValue(), ruleValue.getRuleType())) {
            if (value > ruleValue.getValue()) {
                return true;
            }
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
     *
     * @param ruleValue 规则
     * @param date      比较日期
     * @return
     */
    protected boolean checkRuleValue(RuleValue ruleValue, Date date) {
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
     *
     * @param subUsers 子账号(已经通过roleId过滤)
     * @param ruleRole 规则角色
     * @param jsonData 填充数据
     * @return
     */
    protected void createMessage(List<SubUser> subUsers, DoctorMessageRuleRole ruleRole, String jsonData, Integer eventType, Long businessId) {
        DoctorMessageSearchDto doctorMessageSearchDto = new DoctorMessageSearchDto();
        doctorMessageSearchDto.setFarmId(ruleRole.getFarmId());
        doctorMessageSearchDto.setTemplateId(ruleRole.getTemplateId());
        doctorMessageSearchDto.setBusinessId(businessId);
        List<DoctorMessage> messageList = RespHelper.orServEx(doctorMessageReadService.findMessageListByCriteria(doctorMessageSearchDto));
        if (!messageList.isEmpty()){
            messageList.forEach(doctorMessage -> {
                doctorMessageWriteService.updateMessage(doctorMessage);
            });
            return;
        }
        if (Arguments.isNullOrEmpty(subUsers)){
            return;
        }
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
        DoctorMessage message = DoctorMessage.builder()
                .farmId(ruleRole.getFarmId())
                .ruleId(ruleRole.getRuleId())
                .roleId(ruleRole.getRoleId())
                .templateName(template.getName())
                .templateId(ruleRole.getTemplateId())
                .businessId(businessId)
                .messageTemplate(template.getMessageTemplate())
                .type(template.getType())
                .eventType(eventType)
                .category(template.getCategory())
                .data(jsonData)
                .url(ruleRole.getRule().getUrl())
                .createdBy(template.getUpdatedBy())
                .build();
        // 模板编译
//        try {
//            Map<String, Serializable> jsonContext = MAPPER.readValue(jsonData, JacksonType.MAP_OF_STRING);
//            String content = RespHelper.orServEx(doctorMessageTemplateReadService.getMessageContentWithCache(message.getMessageTemplate(), jsonContext));
//            message.setContent(content != null ? content.trim() : "");
//        } catch (Exception e) {
//            log.error("compile message template failed,cause by {}, template name is {}, json map is {}", Throwables.getStackTraceAsString(e), message.getMessageTemplate(), jsonData);
//        }
        Long messageId = RespHelper.orServEx(doctorMessageWriteService.createMessage(message));
        if (subUsers != null && subUsers.size() > 0) {
            subUsers.stream().map(SubUser::getParentUserId).collect(Collectors.toSet())
                    .forEach(parentId -> {
                        DoctorMessageUser doctorMessageUser = DoctorMessageUser.builder()
                                .userId(parentId)
                                .messageId(messageId)
                                .businessId(businessId)
                                .farmId(ruleRole.getFarmId())
                                .templateId(ruleRole.getTemplateId())
                                .statusSys(DoctorMessageUser.Status.NORMAL.getValue())
                                .statusSms(DoctorMessageUser.Status.NORMAL.getValue())
                                .statusEmail(DoctorMessageUser.Status.NORMAL.getValue())
                                .statusApp(DoctorMessageUser.Status.NORMAL.getValue())
                                .build();
                        doctorMessageUserWriteService.createDoctorMessageUser(doctorMessageUser);
                    });

            // 子账户
            subUsers.stream().forEach(subUser -> {
                // 检查该用户是否含有farm权限
                if (hasUserAuth(subUser.getUserId(), ruleRole.getFarmId())) {
                    DoctorMessageUser doctorMessageUser = DoctorMessageUser.builder()
                            .userId(subUser.getUserId())
                            .messageId(messageId)
                            .businessId(businessId)
                            .farmId(ruleRole.getFarmId())
                            .templateId(ruleRole.getTemplateId())
                            .statusSys(DoctorMessageUser.Status.NORMAL.getValue())
                            .statusSms(DoctorMessageUser.Status.NORMAL.getValue())
                            .statusEmail(DoctorMessageUser.Status.NORMAL.getValue())
                            .statusApp(DoctorMessageUser.Status.NORMAL.getValue())
                            .build();
                    doctorMessageUserWriteService.createDoctorMessageUser(doctorMessageUser);
                }
            });
        }
    }

    /**
     * 获取最新发送的预警或警报的消息
     *
     * @param templateId 规则模板id
     * @param farmId     猪场id
     * @param roleId     角色id
     * @return
     */
    private DoctorMessage getLatestWarnMessage(Long templateId, Long farmId, Long roleId) {
        return RespHelper.orServEx(doctorMessageReadService.findLatestWarnMessage(templateId, farmId, roleId));
    }

    /**
     * 获取最新发送的预警或警报的消息
     *
     * @param templateId 规则模板id
     * @param farmId     猪场id
     * @return
     */
    private DoctorMessage getLatestWarnMessage(Long templateId, Long farmId) {
        return RespHelper.orServEx(doctorMessageReadService.findLatestWarnMessage(templateId, farmId));
    }

    /**
     * 获取最新发送的系统消息(系统消息是对应到模板的)
     *
     * @param templateId
     * @return
     */
    private DoctorMessage getLatestSysMessage(Long templateId) {
        return RespHelper.orServEx(doctorMessageReadService.findLatestSysMessage(templateId));
    }

    /**
     * 检查消息是否在频率范围之内
     *
     * @param message 消息
     * @param rule    规则
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
                if (new DateTime(message.getUpdatedAt())
                        .isAfter(DateTime.now().minusHours(rule.getFrequence()))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取总页数
     *
     * @param total 总数量
     * @param size  批量获取的数量
     * @return
     */
    protected Long getPageSize(Long total, Long size) {
        size = MoreObjects.firstNonNull(size, 100L);
        Long page = 0L;
        if (total != null) {
            if (total % size == 0) {
                page = total / size;
            } else {
                page = total / size + 1;
            }
        }
        return page;
    }
}
