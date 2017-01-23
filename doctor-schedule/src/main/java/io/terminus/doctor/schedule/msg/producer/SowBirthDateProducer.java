package io.terminus.doctor.schedule.msg.producer;

import com.google.common.collect.Maps;
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
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.schedule.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 母猪预产期提示
 * 1. 妊娠检查阳性 (预产期: 最近一次配种时间 + 3月, 当前是页面输入项)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/5
 */
@Component
@Slf4j
public class SowBirthDateProducer extends AbstractJobProducer {

    public SowBirthDateProducer() {
        super(Category.SOW_BIRTHDATE);
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
        DoctorMessageRuleTemplate ruleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
        Map<String, Object> criMap = Maps.newHashMap();
        criMap.put("farmId", ruleRole.getFarmId());
        criMap.put("category", category.getKey());
        // 批量获取母猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.getPigCount(ruleRole.getFarmId(), DoctorPig.PigSex.SOW));
        // 计算size, 分批处理
        Long page = getPageSize(total, 100L);
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            // 过滤出检查阳性的母猪
            pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Pregnancy.getKey(), pigDto.getStatus())).collect(Collectors.toList());
            // 处理每个猪
            for (int j = 0; pigs != null && j < pigs.size(); j++) {
                try {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    //根据用户拥有的猪舍权限过滤拥有user
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    DoctorPigEvent matingPigEvent = getMatingPigEvent(pigDto);
                    // 母猪怀孕天数
                    Double timeDiff = getTimeDiff(new DateTime(matingPigEvent.getEventAt()));

                    ruleValueMap.values().forEach(ruleValue -> {
                        if (checkRuleValue(ruleValue, timeDiff)) {
                            DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                                    .code(pigDto.getPigCode())
                                    .barnId(pigDto.getBarnId())
                                    .barnName(pigDto.getBarnName())
                                    .timeDiff(timeDiff)
                                    .ruleTimeDiff(getRuleTimeDiff(ruleValue, timeDiff))
                                    .reason(ruleValue.getDescribe())
                                    .eventAt(matingPigEvent.getEventAt())
                                    .eventType(PigEvent.TO_FARROWING.getKey())
                                    .ruleValueId(ruleValue.getId())
                                    .url(getPigJumpUrl(pigDto))
                                    .businessId(pigDto.getPigId())
                                    .businessType(DoctorMessage.BUSINESS_TYPE.PIG.getValue())
                                    .operatorId(matingPigEvent.getOperatorId())
                                    .operatorName(matingPigEvent.getOperatorName())
                                    .status(pigDto.getStatus())
                                    .statusName(pigDto.getStatusName())
                                    .build();
                            createMessage(sUsers, ruleRole, messageInfo);
                        }
                    });
                } catch (Exception e) {
                    log.error("[SowBirthDateProduce]-handle.message.failed");
                }
            }
        }
    }

}
