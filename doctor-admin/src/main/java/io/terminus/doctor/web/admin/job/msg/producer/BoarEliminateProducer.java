package io.terminus.doctor.web.admin.job.msg.producer;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.dto.msg.Rule;
import io.terminus.doctor.event.dto.msg.RuleValue;
import io.terminus.doctor.event.dto.msg.SubUser;
import io.terminus.doctor.event.enums.Category;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageRuleRole;
import io.terminus.doctor.web.admin.job.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 公猪应淘汰提示
 * 1. 配种次数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class BoarEliminateProducer extends AbstractJobProducer {

    public BoarEliminateProducer() {
        super(Category.BOAR_ELIMINATE);
    }

    @Override
    protected void message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }
        DoctorPig pig = DoctorPig.builder()
                .farmId(ruleRole.getFarmId())
                .pigType(DoctorPig.PigSex.BOAR.getKey())
                .build();
        // 批量获取公猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.getPigCount(ruleRole.getFarmId(), DoctorPig.PigSex.BOAR));
        // 计算size, 分批处理
        Long page = getPageSize(total, 100L);
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> boarPigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            // 过滤出未离场的公猪
            boarPigs = boarPigs.stream().filter(pigDto ->
                    !Objects.equals(PigStatus.Removal.getKey(), pigDto.getStatus())
            ).collect(Collectors.toList());
            // 处理每个猪
            for (int j = 0; boarPigs != null && j < boarPigs.size(); j++) {
                try {
                    DoctorPigInfoDto pigDto = boarPigs.get(j);
                    //根据用户拥有的猪舍权限过滤拥有user
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    // 公猪的updatedAt与当前时间差 (天)
                    Double timeDiff = getTimeDiff(new DateTime(pigDto.getBirthDay()));
                    if (pigDto.getDoctorPigEvents() == null) {
                        break;
                    }
                    //取出最近一次的采精事件
                    DoctorPigEvent doctorPigEvent = getPigEventByEventType(pigDto.getPigId(), PigEvent.SEMEN.getKey());
                    for (Integer key : ruleValueMap.keySet()) {
                        Boolean isSend = false;
                        RuleValue ruleValue = ruleValueMap.get(key);
                        if (key == 1) {
                            //日龄大于或等于预定值
                            isSend = checkRuleValue(ruleValue, timeDiff);
                        } else if (key == 2) {
                            if (doctorPigEvent != null && StringUtils.isNotBlank(doctorPigEvent.getExtra())) {
                                try {
                                    Map<String, Object> extraMap = MAPPER.readValue(doctorPigEvent.getExtra(), JacksonType.MAP_OF_OBJECT);
                                    Double semenActive = (double) extraMap.get("semenActive");
                                    //精液重量小于预定值
                                    isSend = semenActive < ruleValue.getValue().doubleValue();
                                } catch (Exception e) {
                                    log.error("[BoarEliminateProducer].get.semenActive.fail, eventId:{}", doctorPigEvent.getId());
                                }
                            }
                        } else if (key == 3) {
                            if (doctorPigEvent != null && StringUtils.isNotBlank(doctorPigEvent.getExtra())) {
                                try {
                                    Map<String, Object> extraMap = MAPPER.readValue(doctorPigEvent.getExtra(), JacksonType.MAP_OF_OBJECT);
                                    Double semenActive = (double) extraMap.get("semenWeight");
                                    //精液重量小于预定值
                                    isSend = semenActive < ruleValue.getValue().doubleValue();
                                } catch (Exception e) {
                                    log.error("[BoarEliminateProducer].get.semenWeight.fail, eventId:{}", doctorPigEvent.getId());
                                }

                            }
                        }
                        if (isSend) {
                            DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                                    .code(pigDto.getPigCode())
                                    .barnId(pigDto.getBarnId())
                                    .barnName(pigDto.getBarnName())
                                    .timeDiff(timeDiff)
                                    .ruleTimeDiff(null)
                                    .reason(ruleValue.getDescribe() + ruleValue.getValue().toString())
                                    .eventAt(null)
                                    .eventType(PigEvent.REMOVAL.getKey())
                                    .ruleValueId(ruleValue.getId())
                                    .url(getPigJumpUrl(pigDto))
                                    .businessId(pigDto.getPigId())
                                    .businessType(DoctorMessage.BUSINESS_TYPE.PIG.getValue())
                                    .status(pigDto.getStatus())
                                    .statusName(pigDto.getStatusName())
                                    .build();
                            createMessage(sUsers, ruleRole, messageInfo);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("[BoarEliminateProduce]-message.failed");
                }
            }
        }
    }

}
