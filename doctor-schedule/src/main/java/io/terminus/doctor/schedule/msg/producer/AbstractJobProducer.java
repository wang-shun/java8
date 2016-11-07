package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.producer.AbstractProducer;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.schedule.msg.producer.factory.PigDtoFactory;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: Job端抽象Producer
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/6
 */
@Slf4j
public abstract class AbstractJobProducer extends AbstractProducer {

    protected final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    protected final DoctorPigReadService doctorPigReadService;
    protected final DoctorPigWriteService doctorPigWriteService;

    public AbstractJobProducer(DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                               DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                               DoctorMessageRuleReadService doctorMessageRuleReadService,
                               DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                               DoctorMessageReadService doctorMessageReadService,
                               DoctorMessageWriteService doctorMessageWriteService,
                               DoctorPigReadService doctorPigReadService,
                               DoctorPigWriteService doctorPigWriteService,
                               DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                               Category category) {
        super(doctorMessageTemplateReadService, doctorMessageRuleTemplateReadService, doctorMessageRuleReadService, doctorMessageRuleRoleReadService, doctorMessageReadService, doctorMessageWriteService, category);
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigWriteService = doctorPigWriteService;
    }

    @Override
    protected boolean hasUserAuth(Long userId, Long farmId) {
        if (userId != null && farmId != null) {
            DoctorUserDataPermission doctorUserDataPermission = RespHelper.orServEx(
                    doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (doctorUserDataPermission != null) {
                List<Long> farmIdsList = doctorUserDataPermission.getFarmIdsList();
                return farmIdsList != null && farmIdsList.contains(farmId);
            }
        }
        return true;
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
//                List<DoctorPigEvent> eventList = events.stream().filter(doctorPigEvent -> doctorPigEvent.getEventAt() != null).sorted(Comparator.comparing(DoctorPigEvent::getEventAt).reversed()).collect(Collectors.toList());
//                for (DoctorPigEvent doctorPigEvent : eventList) {
//                    if (Objects.equals(doctorPigEvent.getType(), type)) {
//                        return doctorPigEvent;
//                    }
//                }
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
     *
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
     * 获取剩余的天数
     */
//        private Double getTimeDiff(DateTime warningDate, Integer ruleValue) {
//        long millis = warningDate.plusDays(ruleValue)
//                .minus(DateTime.now().getMillis()).getMillis();
//        return (double) (millis / 86400000);
//    }

    /**
     * 创建消息
     */
    protected void getMessage(DoctorPigInfoDto pigDto, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, Double ruleTimeDiff, String url, Integer eventType, Integer ruleValueId) {
        // 创建消息
        String jumpUrl = url.concat("?pigId=" + pigDto.getPigId() + "&farmId=" + ruleRole.getFarmId());
        Map<String, Object> jsonData = PigDtoFactory.getInstance().createPigMessage(pigDto, timeDiff, ruleTimeDiff, url);
            try {
                createMessage(subUsers, ruleRole, MAPPER.writeValueAsString(jsonData), eventType, pigDto.getPigId(), ruleValueId, jumpUrl);
            } catch (JsonProcessingException e) {
                log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
            }
    }

    /**
     * 获取与规则时间差
     *
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
}
