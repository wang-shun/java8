package io.terminus.doctor.schedule.msg.producer;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
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
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by xiao on 16/8/31.
 */
@Slf4j
@Component
public class SowBackFatProducer extends AbstractJobProducer {

    @Autowired
    public SowBackFatProducer(DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                              DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                              DoctorMessageRuleReadService doctorMessageRuleReadService,
                              DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                              DoctorMessageReadService doctorMessageReadService,
                              DoctorMessageWriteService doctorMessageWriteService,
                              DoctorPigReadService doctorPigReadService, DoctorPigWriteService doctorPigWriteService,
                              DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        super(doctorMessageTemplateReadService,
                doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                doctorPigReadService, doctorPigWriteService,
                doctorUserDataPermissionReadService,
                Category.SOW_BACK_FAT);
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {

        log.info("背膘提示消息产生 --- SowBackFatProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();
        handleMessages(ruleRole.getRule(), ruleRole.getTemplateId(), ruleRole.getFarmId(), true, messages, ruleRole, subUsers);
        log.info("背膘提示消息产生 --- SowBackFatProducer  结束执行, 产生 {} 条消息", messages.size());
        return messages;
    }

    @Override
    protected void recordPigMessages(DoctorMessageRule messageRule) {
        handleMessages(messageRule.getRule(), messageRule.getTemplateId(), messageRule.getFarmId(), false, null, null, null);
    }

    private void handleMessages(Rule rule, Long tplId, Long farmId, boolean isMessage, List<DoctorMessage> messages, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }
        DoctorMessageRuleTemplate ruleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(tplId));

        if (StringUtils.isNotBlank(rule.getChannels())) {
            Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                    DataRange.FARM.getKey(), farmId, DoctorPig.PIG_TYPE.SOW.getKey()));
            // 计算size, 分批处理
            Long page = getPageSize(total, 100L);
            DoctorPig pig = DoctorPig.builder()
                    .farmId(farmId)
                    .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                    .build();
            for (int i = 1; i <= page; i++) {
                List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
                pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Mate.getKey(), pigDto.getStatus())
                        || Objects.equals(PigStatus.Pregnancy.getKey(), pigDto.getStatus())
                        || Objects.equals(PigStatus.Farrow.getKey(), pigDto.getStatus())
                        || Objects.equals(PigStatus.FEED.getKey(), pigDto.getStatus())
                        || Objects.equals(PigStatus.Wean.getKey(), pigDto.getStatus())
                        || Objects.equals(PigStatus.Entry.getKey(), pigDto.getStatus())).collect(Collectors.toList());
                // 处理每个猪
                for (int j = 0; pigs != null && j < pigs.size(); j++) {
                    try {
                        DoctorPigInfoDto pigDto = pigs.get(j);
                        //根据猪场权限过滤用户
                        List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                        for (Integer key : ruleValueMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toSet())) {
                            Double timeDiff = null;
                            Boolean isSend = false;
                            RuleValue ruleValue = ruleValueMap.get(key);
                            DoctorPigEvent matingPigEvent = getMatingPigEvent(pigDto);
                            if (matingPigEvent == null) {
                                break;
                            }
                            if (key == 1 || key == 2 || key == 3) {
                                timeDiff = getTimeDiff(new DateTime(matingPigEvent.getEventAt()));
                                if (filterPigCondition(pigDto, matingPigEvent)) {
                                    isSend = checkRuleValue(ruleValue, timeDiff);
                                }
                            } else {
                                DoctorPigEvent weanPigEvent = getPigEventByEventType(pigDto.getDoctorPigEvents(), PigEvent.WEAN.getKey());
                                if (weanPigEvent != null && filterPigCondition(pigDto, weanPigEvent)) {
                                    timeDiff = getTimeDiff(new DateTime(weanPigEvent.getEventAt()));
                                    isSend = true;
                                }
                            }
                            if (isSend) {
                                if (!isMessage && Objects.equals(ruleTemplate.getType(), DoctorMessageRuleTemplate.Type.WARNING.getValue())) {
                                    recordPigMessage(pigDto, PigEvent.CONDITION, null, ruleValue.getValue().intValue(), PigStatus.Mate, PigStatus.Pregnancy, PigStatus.Farrow, PigStatus.FEED, PigStatus.Wean, PigStatus.Entry);
                                } else if (isMessage) {
                                    pigDto.setEventDate(matingPigEvent.getEventAt());
                                    pigDto.setOperatorName(matingPigEvent.getOperatorName());
                                    pigDto.setRuleValueId(key);
                                    messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, sUsers, timeDiff, rule.getUrl(), PigEvent.CONDITION.getKey()));
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.error("[SowBackFatProduce]-handle.message.failed");
                    }
                }
            }
        }
    }

    /**
     * 构建过滤猪的条件
     *
     * @param pigDto
     * @return Boolean
     */
    private Boolean filterPigCondition(DoctorPigInfoDto pigDto, DoctorPigEvent event) {
        if (!Arguments.isNullOrEmpty(pigDto.getDoctorPigEvents())) {
            List<DoctorPigEvent> list = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> new DateTime(doctorPigEvent.getEventAt()).isAfter(new DateTime(event.getEventAt())) && (Objects.equals(doctorPigEvent.getType(), PigEvent.CONDITION.getKey()))).collect(Collectors.toList());
            if (list.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
