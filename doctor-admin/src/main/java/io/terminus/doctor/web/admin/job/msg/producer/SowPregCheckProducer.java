package io.terminus.doctor.web.admin.job.msg.producer;

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
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.web.admin.job.msg.dto.DoctorMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 母猪需妊娠检查提示
 * <p>
 * 1. 已配种日期
 * <p>
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/3
 */
@Component
@Slf4j
public class SowPregCheckProducer extends AbstractJobProducer {


    public SowPregCheckProducer() {
        super(Category.SOW_PREGCHECK);
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
        // 批量获取母猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.getPigCount(ruleRole.getFarmId(), DoctorPig.PigSex.SOW));
        Map<String, Object> criMap = Maps.newHashMap();
        criMap.put("farmId", ruleRole.getFarmId());
        criMap.put("category", category.getKey());
        List<DoctorMessageRule> doctorMessageRules = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByCriteria(criMap));
        Map<Integer, DoctorMessageRule> doctorMessageRuleMap = doctorMessageRules.stream().collect(Collectors.toMap(DoctorMessageRule::getType, v->v));
        DoctorMessageRule warnRule = doctorMessageRuleMap.get(DoctorMessageRuleTemplate.Type.WARNING.getValue());
        DoctorMessageRule errorRule = doctorMessageRuleMap.get(DoctorMessageRuleTemplate.Type.ERROR.getValue());
        // 计算size, 分批处理
        Long page = getPageSize(total, 100L);
        for (int i = 1; i <= page; i++) {
            List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
            // 过滤出已经配种的母猪
            pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Mate.getKey(), pigDto.getStatus())).collect(Collectors.toList());
            // 处理每个猪
            for (int j = 0; pigs != null && j < pigs.size(); j++) {
                try {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    //根据用户拥有的猪舍权限过滤拥有user
                    List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                    // 母猪的updatedAt与当前时间差 (天)
                    DoctorPigEvent matingPigEvent = getMatingPigEvent(pigDto);
                    Double timeDiff = getTimeDiff(new DateTime(matingPigEvent.getEventAt()));

                    RuleValue ruleValue = ruleValueMap.get(1);
                    Double ruleTimeDiff = getRuleTimeDiff(ruleValue, timeDiff);
                    Boolean isSend = checkRuleValue(ruleValue, timeDiff);
                    if (Objects.equals(ruleTemplate.getType(), DoctorMessageRuleTemplate.Type.WARNING.getValue())) {
                        isSend =  isSend && !checkRuleValue(errorRule.getRule().getValues().get(0), timeDiff);
                    }else {
                        ruleTimeDiff = getRuleTimeDiff(warnRule.getRule().getValues().get(0), timeDiff);
                    }
                    if (isSend) {
                        DoctorMessageInfo messageInfo = DoctorMessageInfo.builder()
                                .code(pigDto.getPigCode())
                                .barnId(pigDto.getBarnId())
                                .barnName(pigDto.getBarnName())
                                .timeDiff(timeDiff)
                                .ruleTimeDiff(ruleTimeDiff)
                                .reason(ruleValue.getDescribe())
                                .eventType(PigEvent.PREG_CHECK.getKey())
                                .eventAt(matingPigEvent.getEventAt())
                                .ruleValueId(ruleValue.getId())
                                .url(getPigJumpUrl(pigDto))
                                .businessId(pigDto.getPigId())
                                .businessType(DoctorMessage.BUSINESS_TYPE.PIG.getValue())
                                .status(pigDto.getStatus())
                                .statusName(pigDto.getStatusName())
                                .build();
                        createMessage(sUsers, ruleRole, messageInfo);
                    }
                } catch (Exception e) {
                    log.error("[sowPregCheckProducer]-handle.message.failed");
                }
            }
        }
    }
}
