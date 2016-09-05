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
//        log.info("背膘提示消息产生 --- SowBackFatProducer 开始执行");
//        List<DoctorMessage> messages = Lists.newArrayList();
//
//        Rule rule = ruleRole.getRule();
//        // ruleValue map
//        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
//        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
//            RuleValue ruleValue = rule.getValues().get(i);
//            ruleValueMap.put(ruleValue.getId(), ruleValue);
//        }
//
//        if (StringUtils.isNotBlank(rule.getChannels())) {
//            Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
//                    DataRange.FARM.getKey(), ruleRole.getFarmId(), DoctorPig.PIG_TYPE.SOW.getKey()));
//            // 计算size, 分批处理
//            Long page = getPageSize(total, 100L);
//            DoctorPig pig =DoctorPig.builder()
//                    .farmId(ruleRole.getFarmId())
//                    .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
//                    .build();
//            for (int i = 1; i <= page; i++) {
//                List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
//                pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Mate.getKey(), pigDto.getStatus())
//                        || Objects.equals(PigStatus.Wean.getKey(), pigDto.getStatus())).collect(Collectors.toList());
//                // 处理每个猪
//                for (int j = 0; pigs != null && j < pigs.size(); j++) {
//                    DoctorPigInfoDto pigDto = pigs.get(j);
//                    Double timeDiff = (double) (DateTime.now().minus(pigDto.getUpdatedAt().getTime()).getMillis() / 86400000);
//                    ruleValueMap.keySet().forEach(key -> {
//                        if (ruleValueMap.get(key) != null) {
//
//                            Boolean isSend = false;
//                            RuleValue ruleValue = ruleValueMap.get(key);
//                            if (key == 1) {
//                                isSend = pigDto.getDateAge() > ruleValue.getValue() + 1;
//                            } else if (key == 2) {
//                                if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
//                                    try {
//                                        Map<String, Object> extraMap = MAPPER.readValue(pigDto.getExtraTrack(), JacksonType.MAP_OF_OBJECT);
//                                        if (StringUtils.isNotBlank((String) extraMap.get("semenWeight"))) {
//                                            Float semenWeight = Float.parseFloat((String) extraMap.get("semenWeight"));
//                                            isSend = semenWeight < ruleValue.getValue();
//                                        }
//                                    } catch (Exception e) {
//                                    }
//
//                                }
//                            } else if (key == 3) {
//                                if (StringUtils.isNotBlank(pigDto.getExtraTrack())) {
//                                    try {
//                                        Map<String, Object> extraMap = MAPPER.readValue(pigDto.getExtraTrack(), JacksonType.MAP_OF_OBJECT);
//                                        if (StringUtils.isNotBlank((String) extraMap.get("semenActive"))) {
//                                            Float semenActive = Float.parseFloat((String) extraMap.get("semenActive"));
//                                            isSend = semenActive < ruleValue.getValue();
//                                        }
//                                    } catch (Exception e) {
//                                    }
//
//                                }
//                            }else {
//
//                            }
//                            if (isSend) {
//                                messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, subUsers, timeDiff, rule.getUrl()));
//                            }
//                        }
//                    });
//
//                }
//            }
//        }
//        log.info("公猪应淘汰消息产生 --- BoarEliminateProducer 结束执行, 产生 {} 条消息", messages.size());
//        return messages;
        return null;
    }

        /**
         * 创建消息
         */
        private List<DoctorMessage> getMessage(DoctorPigInfoDto pigDto, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, Double timeDiff, String url) {
            List<DoctorMessage> messages = Lists.newArrayList();
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
