package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.api.client.util.Lists;
import com.google.common.base.Throwables;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.jsr356.encoders.DoubleEncoder;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Date;
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

    @Override
    protected void recordPigMessages(DoctorMessageRule messageRule) {
        // 子类实现
    }

    /**
     * 记录每只猪的消息提醒
     *
     * @param pigDto      猪只详情
     * @param pigEvent    即将需要执行的事件
     * @param ruleValue   配置的的天数
     * @param pigStatuses 母猪当前的状态
     */
    protected void recordPigMessage(DoctorPigInfoDto pigDto, PigEvent pigEvent, DateTime warningDate, Integer ruleValue, PigStatus... pigStatuses) {
        List statusList = Lists.newArrayList();
        if (pigStatuses != null && pigStatuses.length > 0) {
            for (PigStatus pigStatus : pigStatuses) {
                statusList.add(pigStatus.getKey());
            }
        }

        // 处理消息
        List<DoctorPigMessage> tmpPigMessages = Lists.newArrayList();
        tmpPigMessages.add(DoctorPigMessage.builder()
                .pigId(pigDto.getPigId())
                .eventType(pigEvent.getKey())
                .eventTypeName(pigEvent.getName())
                .status(pigDto.getStatus())
                .timeDiff(getTimeDiff(warningDate, ruleValue))
                .build());

        // 处理存在的消息和过期的消息
        if (StringUtils.isNotBlank(pigDto.getExtraTrackMessage())) {
            try {
                List<DoctorPigMessage> pigMessages = MAPPER.readValue(pigDto.getExtraTrackMessage(), new TypeReference<List<DoctorPigMessage>>() {});
                if (!Objects.isNull(pigMessages)) {
                    pigMessages.stream().filter(doctorPigMessage -> !Objects.equals(doctorPigMessage.getEventType(), pigEvent.getKey()))
                            .forEach(doctorPigMessage -> {
                                if (statusList.contains(doctorPigMessage.getStatus())) {
                                    tmpPigMessages.add(doctorPigMessage);
                                }
                            });
                }
            } catch (Exception e) {
                log.error("format pig message error, cause by {}", Throwables.getStackTraceAsString(e));
            }
        }

        // 保存到数据库
        DoctorPigTrack doctorPigTrack = RespHelper.orServEx(doctorPigReadService.findPigTrackByPigId(pigDto.getPigId()));
        if (doctorPigTrack != null) {
            doctorPigTrack.setExtraMessageList(tmpPigMessages);
            doctorPigWriteService.updatePigTrackExtraMessage(doctorPigTrack);
        }
    }

    /**
     * 获取猪的最近一次初配事件
     * @param pigDto
     * @return
     */
    protected DoctorPigEvent getMatingPigEvent(DoctorPigInfoDto pigDto) {
        try {
            DoctorPigEvent doctorPigEvent = null;
            List<DoctorPigEvent> events = pigDto.getDoctorPigEvents();
            for (int i = events.size() - 1; i > -1; i--) {
                if (Objects.equals(events.get(i).getType(), PigEvent.MATING.getKey())) {
                    doctorPigEvent = events.get(i);
                    if (i - 1 > -1 && Objects.equals(events.get(i).getType(), PigEvent.MATING.getKey())) {
                        doctorPigEvent = events.get(i);
                        if (i - 2 > -1 && Objects.equals(events.get(i).getType(), PigEvent.MATING.getKey())) {
                             return doctorPigEvent = events.get(i);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            return doctorPigEvent;
        }catch (Exception e){
            log.error("get mating date fail");
        }
        return null;
    }

    /**
     * 获取预产期
     * @param pigDto
     */
    protected DateTime getBirthDate(DoctorPigInfoDto pigDto, RuleValue ruleValue) {
        // 获取预产期
        try{
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorMatingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("judgePregDate"));
                if (date != null) {
                    return new DateTime(date);
                } else {
                    // 获取配种日期
                    date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("matingDate"));
                    if (date != null) {
                        // 配种日期 + 3 个月返回
                        return new DateTime(date).plusDays(ruleValue.getLeftValue().intValue());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SowBirthDateProducer] get birth date failed, pigDto is {}", pigDto);
        }
        return new DateTime(pigDto.getUpdatedAt());
    }

    /**
     * 获取到达当前状态的时间
     * @param pigDto
     * @return
     */
    protected DateTime getStatusDate(DoctorPigInfoDto pigDto) {
        try {
            PigStatus STATUS = PigStatus.from(pigDto.getStatus());
            DateTime dateTime = null;
            DoctorPigEvent doctorPigEvent;
            if(STATUS != null) {
                switch (STATUS) {
                    case Wean : case Entry :// 断奶
                        // @see DoctorWeanDto
                        doctorPigEvent = getPigEventByEventType(pigDto.getDoctorPigEvents(), PigEvent.WEAN.getKey());
                        if (doctorPigEvent != null) {
                            dateTime = new DateTime(doctorPigEvent.getEventAt());
                            pigDto.setOperatorName(doctorPigEvent.getOperatorName());
                        }
                        break;
                    case KongHuai: // 空怀
                        // @see DoctorPregChkResultDto
                        doctorPigEvent = getPigEventByEventType(pigDto.getDoctorPigEvents(), PigEvent.PREG_CHECK.getKey());
                        pigDto.setStatusName(PregCheckResult.from(doctorPigEvent.getPregCheckResult()).getDesc());
                        dateTime = new DateTime(doctorPigEvent.getEventAt());
                        break;
                }
            }
            return dateTime;
        } catch (Exception e) {
            log.error("SowPregCheckProducer get status date failed, pigDto is {}", pigDto);
        }
        return null;
    }
    /**
     * 根据猪舍过滤用户
     * @param subUsers
     * @param barnId
     * @return
     */
    protected List<SubUser> filterSubUserBarnId(List<SubUser> subUsers, Long barnId){
        if (Arguments.isNullOrEmpty(subUsers)){
            return Collections.emptyList();
        }
        return subUsers.stream().filter(subUser -> filterCondition(subUser, barnId)).collect(Collectors.toList());
    }

    /**
     * 构建过滤条件
     * @param subUser
     * @param barnId
     * @return
     */
    private Boolean filterCondition(SubUser subUser, Long barnId) {
        return !Arguments.isNullOrEmpty(subUser.getBarnIds()) && subUser.getBarnIds().contains(barnId);
    }

    /**
     * 根据事件类型时间列表中取出最近事件
     * @param events
     * @param type
     * @return
     */
    protected DoctorPigEvent getPigEventByEventType(List<DoctorPigEvent> events, Integer type){
        try {
            if (!Arguments.isNullOrEmpty(events)){
                List<DoctorPigEvent> eventList = events.stream().sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt())).collect(Collectors.toList());
                for (DoctorPigEvent doctorPigEvent : eventList) {
                    if (Objects.equals(doctorPigEvent.getType(), type)){
                        return doctorPigEvent;
                    }
                }
            }
        }catch (Exception e){
            log.error("get.pig.event.by.event.type.fail ");
        }
        return null;
    }

    /**
     * 获取事件发生时间与当前时间差
     * @param eventTime
     * @return 天数
     */
    protected Double getTimeDiff(DateTime eventTime){
        try {
            Long timeDiff = DateTime.now().getMillis() / 86400000 - eventTime.getMillis() / 86400000;
            return (double) timeDiff;
        } catch (Exception e) {
            log.error("get.timeDiff.fail, eventTime {}", eventTime);
        }
        return null;
    }

    /**
     * 获取剩余的天数
     */
        private Double getTimeDiff(DateTime warningDate, Integer ruleValue) {
        long millis = warningDate.plusDays(ruleValue)
                .minus(DateTime.now().getMillis()).getMillis();
        return (double) (millis / 86400000);
    }

    /**
     * 创建消息
     */
    protected List<DoctorMessage> getMessage(DoctorPigInfoDto pigDto, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, String url) {
        List<DoctorMessage> messages = com.google.common.collect.Lists.newArrayList();
        // 创建消息
        Map<String, Object> jsonData = PigDtoFactory.getInstance().createPigMessage(pigDto, timeDiff, url);

        Splitters.COMMA.splitToList(channels).forEach(channel -> {
            try {
                messages.addAll(createMessage(subUsers, ruleRole, Integer.parseInt(channel), MAPPER.writeValueAsString(jsonData)));
            } catch (JsonProcessingException e) {
                log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
            }
        });
        return messages;
    }

}
