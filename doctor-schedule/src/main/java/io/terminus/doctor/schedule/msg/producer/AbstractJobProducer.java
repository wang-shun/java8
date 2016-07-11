package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.api.client.util.Lists;
import com.google.common.base.Throwables;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.producer.AbstractProducer;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    protected void recordPigMessage(DoctorPigInfoDto pigDto, PigEvent pigEvent, Integer ruleValue, PigStatus... pigStatuses) {
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
                .timeDiff(getTimeDiff(pigDto.getUpdatedAt(), ruleValue))
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
     * 获取剩余的天数
     */
    private Double getTimeDiff(Date updatedAt, Integer ruleValue) {
        long millis = new DateTime(updatedAt).plusDays(ruleValue)
                .minus(DateTime.now().getMillis()).getMillis();
        return (double) (millis / 86400000);
    }

    /**
     * 获取猪的妊娠检查日期
     * @param pigDto
     * @return
     */
    protected DateTime getCheckDate(DoctorPigInfoDto pigDto) {
        try{
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorPregChkResultDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("checkDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[SowBirthDateProducer] get check date failed, pigDto is {}", pigDto);
        }
        return new DateTime(pigDto.getUpdatedAt());
    }

    /**
     * 获取预产期
     * @param pigDto
     */
    protected DateTime getBirthDate(DoctorPigInfoDto pigDto) {
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
                        return new DateTime(date).plusMonths(3);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SowBirthDateProducer] get birth date failed, pigDto is {}", pigDto);
        }
        return new DateTime(pigDto.getUpdatedAt());
    }

    /**
     * 获取最近一次配种日期
     */
    protected DateTime getBreedingDate(DoctorPigInfoDto pigDto) {
        // 获取配种日期
        try {
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorMatingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("matingDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[SowNotLitterProducer] get breeding date failed, pigDto is {}", pigDto);
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
            if(StringUtils.isNotBlank(pigDto.getExtraTrack()) && STATUS != null) {
                switch (STATUS) {
                    case Wean:  // 断奶
                        // @see DoctorWeanDto
                        dateTime = new DateTime(
                                new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("weanDate")));
                        break;
                    case Abortion:  // 流产
                        // @see DoctorAbortionDto
                        dateTime = new DateTime(
                                new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("abortionDate")));
                        break;
                    case KongHuai:case Pregnancy: case Farrow:  // 空怀, 阳性, 待分娩
                        // @see DoctorPregChkResultDto
                        dateTime = new DateTime(
                                new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("checkDate")));
                        break;
                    case Entry: // 待配种
                        // @see DoctorChgLocationDto
                        dateTime = new DateTime(
                                new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("changeLocationDate")));
                        break;
                }
            }
            return dateTime != null ? dateTime : new DateTime(pigDto.getUpdatedAt());
        } catch (Exception e) {
            log.error("[SowNotLitterProducer] get breeding date failed, pigDto is {}", pigDto);
        }
        return null;
    }

    /**
     * 获取分娩时间
     * @param pigDto
     * @return
     */
    protected DateTime getFarrowingDate(DoctorPigInfoDto pigDto) {
        // 获取配种日期
        try {
            if(StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                // @see DoctorFarrowingDto
                Date date = new Date((Long) MAPPER.readValue(pigDto.getExtraTrack(), Map.class).get("farrowingDate"));
                return new DateTime(date);
            }
        } catch (Exception e) {
            log.error("[SowNotLitterProducer] get farrowing date failed, pigDto is {}", pigDto);
        }
        return new DateTime(pigDto.getUpdatedAt());
    }
}
