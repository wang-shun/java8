package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
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
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.schedule.msg.producer.factory.PigDtoFactory;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.core.Augments;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
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

        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        if (StringUtils.isNotBlank(rule.getChannels())) {
            Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                    DataRange.FARM.getKey(), ruleRole.getFarmId(), DoctorPig.PIG_TYPE.SOW.getKey()));
            // 计算size, 分批处理
            Long page = getPageSize(total, 100L);
            DoctorPig pig = DoctorPig.builder()
                    .farmId(ruleRole.getFarmId())
                    .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                    .build();
            for (int i = 1; i <= page; i++) {
                List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
                pigs = pigs.stream().filter(pigDto -> filterPig(pigDto)).collect(Collectors.toList());
                // 处理每个猪
                for (int j = 0; pigs != null && j < pigs.size(); j++) {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    Double timeDiff = getTimeDiff(getMatingDate(pigDto));
                    ruleValueMap.keySet().forEach(key -> {
                        if (ruleValueMap.get(key) != null) {
                            Boolean isSend = false;
                            RuleValue ruleValue = ruleValueMap.get(key);
                            if (key == 1 || key == 2 || key == 3) {
                                if (getMatingDate(pigDto).isAfter(DateTime.now().minusDays(ruleValue.getValue().intValue()))) {
                                    isSend = true;
                                }
                            } else {
                                if (!Arguments.isNullOrEmpty(pigDto.getDoctorPigEvents())) {
                                    DoctorPigEvent event = pigDto.getDoctorPigEvents().stream().max((event1, event2) -> event1.getId().compareTo(event2.getId())).get();
                                    if (Objects.equals(event.getType(), PigEvent.WEAN.getKey()) && DateTime.now().getMillis() / 86400000 == event.getEventAt().getTime() / 86400000) {
                                        isSend = true;
                                    }
                                }

                            }
                            if (isSend) {
                                messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, sUsers, timeDiff, rule.getUrl(), ruleValue.getDescribe()+ruleValue.getValue()));
                            }
                        }
                    });

                }
            }
        }
        log.info("背膘提示消息产生 --- SowBackFatProducer  结束执行, 产生 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 创建消息
     */
    private List<DoctorMessage> getMessage(DoctorPigInfoDto pigDto, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, String url, String reason) {
        List<DoctorMessage> messages = Lists.newArrayList();
        // 创建消息
        Map<String, Object> jsonData = PigDtoFactory.getInstance().createPigMessage(pigDto, timeDiff, url);
        jsonData.put("reason", reason);
        Splitters.COMMA.splitToList(channels).forEach(channel -> {
            try {
                messages.addAll(createMessage(subUsers, ruleRole, Integer.parseInt(channel), MAPPER.writeValueAsString(jsonData)));
            } catch (JsonProcessingException e) {
                log.error("message produce error, cause by {}", Throwables.getStackTraceAsString(e));
            }
        });
        return messages;
    }

    private Boolean filterPig(DoctorPigInfoDto pigDto) {
        Boolean result = false;
        if (!Arguments.isNullOrEmpty(pigDto.getDoctorPigEvents())) {
            for (DoctorPigEvent doctorPigEvent : pigDto.getDoctorPigEvents()) {
                if (Objects.equals(doctorPigEvent.getType(), PigEvent.MATING.getKey())) {
                    result = true;
                }
                if (Objects.equals(doctorPigEvent.getType(), PigEvent.CONDITION.getKey())) {
                    result = false;
                }
            }
        }
        return result;
    }
}
