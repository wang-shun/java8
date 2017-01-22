package io.terminus.doctor.schedule.msg.producer;

import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.schedule.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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

    public SowBackFatProducer() {
        super(Category.SOW_BACK_FAT);
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
                .pigType(DoctorPig.PigSex.SOW.getKey())
                .build();
        // 批量获取母猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.getPigCount(ruleRole.getFarmId(), DoctorPig.PigSex.SOW));
        // 计算size, 分批处理
        Long page = getPageSize(total, 100L);
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Mate.getKey(), pigDto.getStatus())
                    || Objects.equals(PigStatus.Pregnancy.getKey(), pigDto.getStatus())
                    || Objects.equals(PigStatus.Farrow.getKey(), pigDto.getStatus())
                    || Objects.equals(PigStatus.FEED.getKey(), pigDto.getStatus())
                    || Objects.equals(PigStatus.Wean.getKey(), pigDto.getStatus())
                    || Objects.equals(PigStatus.KongHuai.getKey(), pigDto.getStatus())).collect(Collectors.toList());
            // 处理每个猪
            for (int j = 0; pigs != null && j < pigs.size(); j++) {
                try {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    //根据猪场权限过滤用户
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    Boolean isSend = false;
                    DoctorPigEvent matingPigEvent = getMatingPigEvent(pigDto);
                    Double timeDiff;
                    if (matingPigEvent == null) {
                        break;
                    }
                    List<Integer> keyList = ruleValueMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                    for (Integer key : keyList) {
                        RuleValue ruleValue = ruleValueMap.get(key);
                        timeDiff = getTimeDiff(new DateTime(matingPigEvent.getEventAt()));
                        if (key == 1 || key == 2 || key == 3) {
                            if (checkRuleValue(ruleValue, timeDiff) && filterPigCondition(pigDto, new DateTime(matingPigEvent.getEventAt()).plusDays(ruleValue.getValue().intValue()), PigEvent.CONDITION)) {
                                isSend = true;
                            }
                        } else {
                            DoctorPigEvent pigEvent = getPigEventByEventType(pigDto.getPigId(), PigEvent.WEAN.getKey());
                            if (pigEvent != null && filterPigCondition(pigDto, new DateTime(pigEvent.getEventAt()), PigEvent.CONDITION)){
                                if (!filterPigCondition(pigDto, new DateTime(pigEvent.getEventAt()), PigEvent.MATING) && checkRuleValue(ruleValueMap.get(1), timeDiff)){
                                    continue;
                                } else{
                                    timeDiff = getTimeDiff(new DateTime(pigEvent.getEventAt()));
                                    isSend = true;
                                }

                            }
                        }
                        if (isSend) {
                            DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                                    .barnId(pigDto.getBarnId())
                                    .barnName(pigDto.getBarnName())
                                    .timeDiff(timeDiff)
                                    .ruleTimeDiff(getRuleTimeDiff(ruleValue, timeDiff))
                                    .reason(ruleValue.getDescribe())
                                    .eventAt(matingPigEvent.getEventAt())
                                    .eventType(PigEvent.CONDITION.getKey())
                                    .ruleValueId(ruleValue.getId())
                                    .url(getPigJumpUrl(pigDto, ruleRole))
                                    .businessId(pigDto.getPigId())
                                    .businessType(DoctorMessage.BUSINESS_TYPE.PIG.getValue())
                                    .operatorId(matingPigEvent.getOperatorId())
                                    .operatorName(matingPigEvent.getOperatorName())
                                    .status(pigDto.getStatus())
                                    .statusName(pigDto.getStatusName())
                                    .build();
                            createMessage(sUsers, ruleRole, messageInfo);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("[SowBackFatProduce]-handle.message.failed");
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
    private Boolean filterPigCondition(DoctorPigInfoDto pigDto, DateTime time, PigEvent pigEvent) {
        if (!Arguments.isNullOrEmpty(pigDto.getDoctorPigEvents())) {
            List<DoctorPigEvent> list = pigDto.getDoctorPigEvents().stream().filter(doctorPigEvent -> new DateTime(doctorPigEvent.getEventAt()).isAfter(time) && (Objects.equals(doctorPigEvent.getType(), pigEvent.getKey()))).collect(Collectors.toList());
            if (list.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
