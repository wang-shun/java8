package io.terminus.doctor.schedule.msg.producer;

import com.google.common.collect.Maps;
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
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public SowBirthDateProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
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
                Category.SOW_BIRTHDATE);
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
                .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                .build();
        DoctorMessageRuleTemplate ruleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(ruleRole.getTemplateId()));
        Map<String, Object> criMap = Maps.newHashMap();
        criMap.put("farmId", ruleRole.getFarmId());
        criMap.put("category", category.getKey());
        List<DoctorMessageRule> doctorMessageRules = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByCriteria(criMap));
        Map<Integer, DoctorMessageRule> doctorMessageRuleMap = doctorMessageRules.stream().collect(Collectors.toMap(k-> k.getType(), v->v));
        DoctorMessageRule warnRule = doctorMessageRuleMap.get(DoctorMessageRuleTemplate.Type.WARNING.getValue());
        DoctorMessageRule errorRule = doctorMessageRuleMap.get(DoctorMessageRuleTemplate.Type.ERROR.getValue());
        // 批量获取母猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                DataRange.FARM.getKey(), ruleRole.getFarmId(), DoctorPig.PIG_TYPE.SOW.getKey()));
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
                    DoctorPigEvent doctorPigEvent = getMatingPigEvent(pigDto);
                    // 母猪怀孕天数
                    Double timeDiff = getTimeDiff(new DateTime(doctorPigEvent.getEventAt()));

                    ruleValueMap.values().forEach(ruleValue -> {
                        Boolean isSend = checkRuleValue(ruleValue, timeDiff);
                        if (Objects.equals(ruleTemplate.getType(), DoctorMessageRuleTemplate.Type.WARNING.getValue())) {
                            isSend = isSend && !checkRuleValue(errorRule.getRule().getValues().get(0), timeDiff);
                        }
                        if (isSend) {
                            pigDto.setEventDate(doctorPigEvent.getEventAt());
                            pigDto.setOperatorName(doctorPigEvent.getOperatorName());
                            getMessage(pigDto, ruleRole, sUsers, timeDiff, timeDiff, rule.getUrl(), PigEvent.TO_FARROWING.getKey(), ruleValue.getId());
                        }
                    });
                } catch (Exception e) {
                    log.error("[SowBirthDateProduce]-handle.message.failed");
                }
            }
        }
    }

//    /**
//     * 获取母猪提示里的时间差
//     * @param events
//     * @param value
//     * @return
//     */
//    private Double getBirthDateTimeDiff(List<DoctorPigEvent> events, Integer value){
//        Date pregCheckDate = getPigEventByEventType(events, PigEvent.PREG_CHECK.getKey()).getEventAt();
//        Double diffTime = getTimeDiff(new DateTime(pregCheckDate));
//        List<DoctorPigEvent> afterPregCheckEvents = events.stream().filter(doctorPigEvent -> doctorPigEvent.getEventAt().after(pregCheckDate)).collect(Collectors.toList());
//        DoctorPigEvent toFarrowingEvent = getPigEventByEventType(afterPregCheckEvents, PigEvent.TO_FARROWING.getKey());
//        Double warnDiffTime;
//        if (toFarrowingEvent == null){
//             warnDiffTime = value - diffTime;
//        }else {
//             warnDiffTime = (double)((DateTime.now().getMillis() + 28800000) / 86400000 - (toFarrowingEvent.getEventAt().getTime() + 28800000) / 86400000);
//        }
//        return warnDiffTime;
//    }
}
