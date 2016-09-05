package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 公猪应淘汰提示
 *          1. 配种次数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class BoarEliminateProducer extends AbstractJobProducer {

    @Autowired
    public BoarEliminateProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                                 DoctorMessageRuleReadService doctorMessageRuleReadService,
                                 DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                                 DoctorMessageReadService doctorMessageReadService,
                                 DoctorMessageWriteService doctorMessageWriteService,
                                 DoctorPigReadService doctorPigReadService,
                                 DoctorPigWriteService doctorPigWriteService,
                                 DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                                 DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        super(doctorMessageTemplateReadService,
                doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                doctorPigReadService,
                doctorPigWriteService,
                doctorUserDataPermissionReadService,
                Category.BOAR_ELIMINATE);
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("公猪应淘汰消息产生 --- BoarEliminateProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 批量获取公猪信息
            Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                    DataRange.FARM.getKey(), ruleRole.getFarmId(), DoctorPig.PIG_TYPE.BOAR.getKey()));
            // 计算size, 分批处理
            Long page = getPageSize(total, 100L);
            DoctorPig pig = DoctorPig.builder()
                    .farmId(ruleRole.getFarmId())
                    .pigType(DoctorPig.PIG_TYPE.BOAR.getKey())
                    .build();
            for (int i = 1; i <= page; i++) {
                List<DoctorPigInfoDto> boarPigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
                // 过滤出未离场的公猪
                boarPigs = boarPigs.stream().filter(pigDto ->
                        !Objects.equals(PigStatus.Removal.getKey(), pigDto.getStatus())
                ).collect(Collectors.toList());
                // 处理每个猪
                for (int j = 0; boarPigs != null && j < boarPigs.size(); j++) {
                    DoctorPigInfoDto pigDto = boarPigs.get(j);
                    // 公猪的updatedAt与当前时间差 (天)
                    Double timeDiff = (double) (DateTime.now().minus(pigDto.getUpdatedAt().getTime()).getMillis() / 86400000);
                    ruleValueMap.keySet().forEach(key -> {
                        if (ruleValueMap.get(key) != null) {

                            Boolean isSend = false;
                            RuleValue ruleValue = ruleValueMap.get(key);
                            if (key == 1) {
                                //日龄大于或等于预定值
                                isSend = pigDto.getDateAge() > ruleValue.getValue().intValue() - 1;
                            } else if (key == 2) {
                                if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                                    try {
                                        Map<String, Object> extraMap = MAPPER.readValue(pigDto.getExtraTrack(), JacksonType.MAP_OF_OBJECT);
                                        if (StringUtils.isNotBlank((String) extraMap.get("semenActive"))) {
                                            Float semenActive = Float.parseFloat((String) extraMap.get("semenActive"));
                                            //精液活性小于预定值
                                            isSend = semenActive < ruleValue.getValue().floatValue();
                                        }
                                    } catch (Exception e) {
                                        log.error("[BoarEliminateProducer].get.semenActive.fail, pigDto", pigDto);
                                    }

                                }
                            } else if (key == 3) {
                                if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
                                    try {
                                        Map<String, Object> extraMap = MAPPER.readValue(pigDto.getExtraTrack(), JacksonType.MAP_OF_OBJECT);
                                        if (StringUtils.isNotBlank((String) extraMap.get("semenWeight"))) {
                                            Float semenWeight = Float.parseFloat((String) extraMap.get("semenWeight"));
                                            //精液重量小于预定值
                                            isSend = semenWeight < ruleValue.getValue().floatValue();
                                        }
                                    } catch (Exception e) {
                                        log.error("[BoarEliminateProducer].get.semenWeight.fail, pigDto", pigDto);
                                    }

                                }
                            }
                            if (isSend) {
                                messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, subUsers, timeDiff, rule.getUrl(), ruleValue.getDescribe() + ruleValue.getValue().toString()));
                            }
                        }
                    });

                }
            }
        }

        log.info("公猪应淘汰消息产生 --- BoarEliminateProducer 结束执行, 产生 {} 条消息", messages.size());
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
}
