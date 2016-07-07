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
import io.terminus.doctor.msg.producer.AbstractProducer;
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

import java.util.Date;
import java.util.List;
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
        /*cif (userId != null && farmId != null) {
            DoctorUserDataPermission doctorUserDataPermission = RespHelper.orServEx(
                    doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (doctorUserDataPermission != null) {
                List<Long> farmIdsList = doctorUserDataPermission.getFarmIdsList();
                return farmIdsList != null && farmIdsList.contains(farmId);
            }
        }*/
        return true;
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

}
