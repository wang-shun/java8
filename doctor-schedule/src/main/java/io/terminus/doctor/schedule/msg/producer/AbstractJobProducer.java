package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
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
import io.terminus.doctor.schedule.msg.producer.factory.PigDtoFactory;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Desc: Job端抽象Producer
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/6
 */
@Slf4j
public abstract class AbstractJobProducer {

    protected ObjectMapper MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    @Autowired
    protected DoctorMessageTemplateReadService doctorMessageTemplateReadService;
    @Autowired
    protected DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;
    @Autowired
    protected DoctorMessageRuleReadService doctorMessageRuleReadService;
    @Autowired
    protected DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService;
    @Autowired
    protected DoctorMessageReadService doctorMessageReadService;
    @Autowired
    protected DoctorMessageWriteService doctorMessageWriteService;
    @Autowired
    protected DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @Autowired
    protected DoctorPigReadService doctorPigReadService;
    @Autowired
    protected DoctorPigWriteService doctorPigWriteService;
    @Autowired
    protected DoctorMessageUserWriteService doctorMessageUserWriteService;

    @Autowired
    private PrimaryUserReadService primaryUserReadService;

    @Value("${msg.jumpUrl.pig.sow}")
    protected String sowPigDetailUrl;

    @Value("${msg.jumpUrl.pig.boar}")
    protected String boarPigDetailUrl;

    @Value("${msg.jumpUrl.group}")
    protected String groupDetailUrl;

    protected Category category;
    @Autowired
    public AbstractJobProducer(Category category){
        this.category = category;
    }

    /**
     * 产生消息
     */
    public void produce() {
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
                log.info("[AbstractJobProducer] {} -> 系统消息产生, starting......", ruleTemplate.getName());
                DoctorMessageRuleRole ruleRole = DoctorMessageRuleRole.builder()
                        .templateId(ruleTemplate.getId())
                        .ruleValue(ruleTemplate.getRuleValue())
                        .build();
                // 获取信息 (针对所有的角色/user)
                message(ruleRole, getUsersHasFarm(null));
                stopwatch.stop();
                log.info("[AbstractJobProducer] {} -> 系统消息产生正常结束, 耗时 {}ms end......", ruleTemplate.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }

            // 2. 如果是预警或警报消息
            else {
                Stopwatch stopWatch = Stopwatch.createStarted();
                log.info("[AbstractJobProducer] {} -> 预警消息产生, starting......", ruleTemplate.getName());

                // > 记录对应每个用户的消息
                List<DoctorMessageRule> messageRules = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByTplId(ruleTemplate.getId()));
                messageRules.forEach(messageRule -> createWarnMessageByMessageRule(messageRule));
                stopWatch.stop();
                log.info("[AbstractJobProducer] {} -> 预警消息产生结束, 耗时 {}ms, ending......", ruleTemplate.getName(), stopWatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * 产生某一规则预警消息
     * @param messageRule
     */
    public void createWarnMessageByMessageRule(DoctorMessageRule messageRule) {
        //将之前消息删除
        DoctorMessageSearchDto dto = new DoctorMessageSearchDto();
        dto.setRuleId(messageRule.getId());
        deleteMessages(dto);

        if (!Objects.equals(messageRule.getStatus(), DoctorMessageRule.Status.NORMAL.getValue())) {
            return;
        }
        DoctorMessageRuleRole ruleRole = DoctorMessageRuleRole.builder()
                .ruleId(messageRule.getId())
                .templateId(messageRule.getTemplateId())
                .farmId(messageRule.getFarmId())
                .ruleValue(messageRule.getRuleValue())
                .build();
        message(ruleRole, getUsersHasFarm(messageRule.getFarmId()));
    }

    /**
     * 获取拥有猪场权限的用户列表(farmId为null,返回全部活跃用户)
     * @param farmId 猪场Id
     * @return
     */
    private List<SubUser> getUsersHasFarm(Long farmId){
        List<SubUser> subUsers = Lists.newArrayList();
        List<Long> userIds = Lists.newArrayList();
        List<Sub> subs = RespHelper.orServEx(primaryUserReadService.findAllActiveSubs());
        if (!Arguments.isNullOrEmpty(subs)){
            userIds.addAll(subs.stream().map(sub -> sub.getUserId()).collect(Collectors.toList()));
        }
        List<PrimaryUser> primaryUsers = RespHelper.orServEx(primaryUserReadService.findAllPrimaryUser());
        if (!Arguments.isNullOrEmpty(primaryUsers)){
            userIds.addAll(primaryUsers.stream().map(sub -> sub.getUserId()).collect(Collectors.toList()));
        }
        userIds.forEach(userId -> {
            SubUser subUser = SubUser.builder()
                    .userId(userId)
                    .farmIds(Lists.newArrayList())
                    .barnIds(Lists.newArrayList())
                    .build();
            if (farmId == null){
                subUsers.add(subUser);
            }
            else {
                // 获取猪场权限
                DoctorUserDataPermission dataPermission = RespHelper.orServEx(
                        doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
                if (dataPermission != null) {
                    dataPermission.setFarmIds(dataPermission.getFarmIds());
                    if (dataPermission.getFarmIdsList().contains(farmId)){
                        dataPermission.setBarnIds(dataPermission.getBarnIds());
                        subUser.getBarnIds().addAll(dataPermission.getBarnIdsList());
                        subUsers.add(subUser);
                    }
                }
            }

        });
        return subUsers;
    }


    /**
     * 创建DoctorMessage对象
     * @param subUsers 子账号(已经通过roleId过滤)
     * @param ruleRole 规则角色
     * @param jsonData 填充数据
     */
    protected void createMessage(List<SubUser> subUsers, DoctorMessageRuleRole ruleRole, String jsonData, Integer eventType, Long businessId, Integer businessType, Integer ruleValueId, String url) {
        DoctorMessageRuleTemplate template = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
        //1.当消息无人有权限时直接返回
        if (Arguments.isNullOrEmpty(subUsers)){
            return;
        }
        //2.产生新消息
        DoctorMessage message = DoctorMessage.builder()
                .farmId(ruleRole.getFarmId())
                .ruleId(ruleRole.getRuleId())
                .roleId(ruleRole.getRoleId())
                .templateName(template.getName())
                .templateId(ruleRole.getTemplateId())
                .businessId(businessId)
                .businessType(businessType)
                .ruleValueId(ruleValueId)
                .messageTemplate(template.getMessageTemplate())
                .type(template.getType())
                .eventType(eventType)
                .category(template.getCategory())
                .data(jsonData)
                .url(url)
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

        //3.关联用户
        subUsers.forEach(subUser -> {
            DoctorMessageUser doctorMessageUser = DoctorMessageUser.builder()
                    .userId(subUser.getUserId())
                    .messageId(messageId)
                    .businessId(businessId)
                    .ruleValueId(ruleValueId)
                    .farmId(ruleRole.getFarmId())
                    .templateId(ruleRole.getTemplateId())
                    .statusSys(DoctorMessageUser.Status.NORMAL.getValue())
                    .statusSms(DoctorMessageUser.Status.NORMAL.getValue())
                    .statusEmail(DoctorMessageUser.Status.NORMAL.getValue())
                    .statusApp(DoctorMessageUser.Status.NORMAL.getValue())
                    .build();
            doctorMessageUserWriteService.createDoctorMessageUser(doctorMessageUser);
        });
    }

    /**
     * 获取总页数
     *
     * @param total 总数量
     * @param size  批量获取的数量
     * @return Long
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

    /**
     * 根据条件删除消息
     * @param doctorMessageSearchDto 查询条件
     */
    private void deleteMessages(DoctorMessageSearchDto doctorMessageSearchDto) {
        while (true) {
            List<Long> messageIds = RespHelper.orServEx(doctorMessageReadService.pagingWarnMessages(doctorMessageSearchDto, 1, 1000)).getData().stream().map(DoctorMessage::getId).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(messageIds)){
                doctorMessageWriteService.deleteMessagesByIds(messageIds);
                doctorMessageUserWriteService.deletesByMessageIds(messageIds);
            }
            if (messageIds.size() < 1000) {
                break;
            }
        }
    }

    /**
     * 获取猪的最近一次初配事件
     *
     * @param pigDto
     * @return
     */
    protected DoctorPigEvent getMatingPigEvent(DoctorPigInfoDto pigDto) {
        try {
            List<DoctorPigEvent> eventList = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> doctorPigEvent.getEventAt() != null).sorted(Comparator.comparing(DoctorPigEvent::getEventAt).reversed()).collect(Collectors.toList());
            DoctorPigEvent doctorPigEvent = null;
            Boolean flag = false;
            for (DoctorPigEvent event : eventList) {
                if (flag && !Objects.equals(event.getType(), PigEvent.MATING.getKey())) {
                    break;
                }
                if (Objects.equals(event.getType(), PigEvent.MATING.getKey())) {
                    flag = true;
                    doctorPigEvent = event;
                }
            }
            return doctorPigEvent;
        } catch (Exception e) {
            log.error("get mating date fail");
        }
        return null;
    }

    /**
     * 获取到达当前状态的时间
     *
     * @param pigDto
     * @return
     */
    protected DateTime getStatusDate(DoctorPigInfoDto pigDto) {
        try {
            PigStatus STATUS = PigStatus.from(pigDto.getStatus());
            DateTime dateTime = null;
            DoctorPigEvent doctorPigEvent = null;
            if (STATUS != null) {
                switch (STATUS) {
                    case Entry:// 进场
                        doctorPigEvent = getPigEventByEventType(pigDto.getDoctorPigEvents(), PigEvent.ENTRY.getKey());
                        dateTime = new DateTime(doctorPigEvent.getEventAt());
                        break;
                    case Wean: //断奶
                        doctorPigEvent = getLeadToWeanEvent(pigDto.getDoctorPigEvents());
                        dateTime = new DateTime(doctorPigEvent.getEventAt());
                        break;
                    case KongHuai: // 空怀
                        doctorPigEvent = getPigEventByEventType(pigDto.getDoctorPigEvents(), PigEvent.PREG_CHECK.getKey());
                        pigDto.setStatusName(PregCheckResult.from(doctorPigEvent.getPregCheckResult()).getDesc());
                        dateTime = new DateTime(doctorPigEvent.getEventAt());
                        break;
                }
            }
            pigDto.setOperatorName(doctorPigEvent.getOperatorName());
            return dateTime;
        } catch (Exception e) {
            log.error("SowPregCheckProducer get status date failed, pigDto is {}", pigDto);
        }
        return null;
    }

    /**
     * 获取导致断奶的事件
     * @param events
     * @return
     */
    protected DoctorPigEvent getLeadToWeanEvent(List<DoctorPigEvent> events){
        try {
            List<DoctorPigEvent> tempList =  events.stream().filter(doctorPigEvent -> doctorPigEvent.getEventAt() !=null &&
                    ((!Objects.equals(doctorPigEvent.getPigStatusBefore(), PigStatus.Wean.getKey()) && Objects.equals(doctorPigEvent.getPigStatusAfter(), PigStatus.Wean.getKey())) || Objects.equals(doctorPigEvent.getType(), PigEvent.WEAN.getKey()))).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(tempList)){
                return tempList.stream().max(Comparator.comparing(DoctorPigEvent::getEventAt)).get();
            }
        } catch (Exception e){
            log.error(" get.lead.to.wean.event.failed ");
        }
        return null;
    }
    /**
     * 根据猪舍过滤用户
     *
     * @param subUsers
     * @param barnId
     * @return
     */
    protected List<SubUser> filterSubUserBarnId(List<SubUser> subUsers, Long barnId) {
        if (Arguments.isNullOrEmpty(subUsers)) {
            return Collections.emptyList();
        }
        return subUsers.stream().filter(subUser -> filterCondition(subUser, barnId)).collect(Collectors.toList());
    }

    /**
     * 构建过滤条件
     *
     * @param subUser
     * @param barnId
     * @return
     */
    private Boolean filterCondition(SubUser subUser, Long barnId) {
        return !Arguments.isNullOrEmpty(subUser.getBarnIds()) && subUser.getBarnIds().contains(barnId);
    }

    /**
     * 根据事件类型时间列表中取出最近事件
     *
     * @param events
     * @param type
     * @return DoctorPigEvent
     */
    protected DoctorPigEvent getPigEventByEventType(List<DoctorPigEvent> events, Integer type) {
        try {
            if (Arguments.isNullOrEmpty(events)){
                return null;
            }
            List<DoctorPigEvent> eventList = events.stream().filter(doctorPigEvent -> (doctorPigEvent.getEventAt() != null) && Objects.equals(doctorPigEvent.getType(), type)).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(eventList)) {
                return eventList.stream().max(Comparator.comparing(DoctorPigEvent::getEventAt)).get();
            }
        } catch (Exception e) {
            log.error("get.pig.event.by.event.type.failed");
        }
        return null;
    }

    /**
     * 获取最新的猪群事件
     * @param events
     * @param type
     * @return
     */
    protected DoctorGroupEvent getLastGroupEventByEventType(List<DoctorGroupEvent> events, Integer type) {
        try {
            if (Arguments.isNullOrEmpty(events)){
                return null;
            }
            List<DoctorGroupEvent> eventList = events.stream().filter(doctorGroupEvent -> (doctorGroupEvent.getEventAt() != null) && Objects.equals(doctorGroupEvent.getType(), type)).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(events)) {
                return eventList.stream().max(Comparator.comparing(DoctorGroupEvent::getEventAt)).get();
            }
        } catch (Exception e) {
            log.error("get.last.group.event.by.event.type.failed");
        }
        return null;
    }

    /**
     * 获取事件发生时间与当前时间差
     * @param eventTime
     * @return Double
     */
    protected Double getTimeDiff(DateTime eventTime) {
        try {
            Long timeDiff = (DateTime.now().getMillis() + 28800000) / 86400000 - (eventTime.getMillis() + 28800000) / 86400000;
            return (double) timeDiff;
        } catch (Exception e) {
            log.error("get.timeDiff.failed, eventTime {}", eventTime);
        }
        return null;
    }

    /**
     * 创建猪消息
     */
    protected void getMessage(DoctorPigInfoDto pigDto, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, Double ruleTimeDiff, String url, Integer eventType, Integer ruleValueId) {
        // 创建消息
        String jumpUrl = url.concat("?pigId=" + pigDto.getPigId() + "&farmId=" + ruleRole.getFarmId());
        Map<String, Object> jsonData = PigDtoFactory.getInstance().createPigMessage(pigDto, timeDiff, ruleTimeDiff, url);
        try {
            createMessage(subUsers, ruleRole, MAPPER.writeValueAsString(jsonData), eventType, pigDto.getPigId(), DoctorMessage.BUSINESS_TYPE.PIG.getValue(), ruleValueId, jumpUrl);
        } catch (JsonProcessingException e) {
            log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取与规则时间差
     * @param ruleValue
     * @param timeDiff
     * @return
     */
    protected Double getRuleTimeDiff(RuleValue ruleValue, Double timeDiff) {
        if (Objects.equals(ruleValue.getRuleType(), RuleValue.RuleType.VALUE.getValue())) {
            return ruleValue.getValue() - timeDiff;
        } else if (Objects.equals(ruleValue.getRuleType(), RuleValue.RuleType.VALUE_RANGE.getValue())) {
            return ruleValue.getLeftValue() - timeDiff;
        }
        return null;
    }



    /**
     * 消息处理,子类必须实现
     * @param ruleRole
     * @param subUsers
     */
    protected abstract void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers);

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
            if (value >= ruleValue.getValue()) {
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
}
