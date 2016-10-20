package io.terminus.doctor.schedule.msg.producer;

import com.google.api.client.util.Maps;
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
import org.apache.commons.lang3.StringUtils;
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

        log.info("母猪预产期提示消息产生 --- SowBirthDateProducer 开始执行");
        handleMessages(ruleRole.getRule(), ruleRole.getTemplateId(), ruleRole.getFarmId(), true, ruleRole, subUsers);
        log.info("母猪预产期提示消息产生 --- SowBirthDateProducer 结束执行");
    }

    @Override
    protected void recordPigMessages(DoctorMessageRule messageRule) {
        handleMessages(messageRule.getRule(), messageRule.getTemplateId(), messageRule.getFarmId(), false, null, null);
    }

    private void handleMessages(Rule rule, Long tplId, Long farmId, boolean isMessage, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
//        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }
        DoctorMessageRuleTemplate ruleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(tplId));

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 批量获取猪信息
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
                // 过滤出检查阳性的母猪
                pigs = pigs.stream().filter(pigDto -> Objects.equals(PigStatus.Pregnancy.getKey(), pigDto.getStatus())).collect(Collectors.toList());
                // 处理每个猪
                for (int j = 0; pigs != null && j < pigs.size(); j++) {
                    try {
                        DoctorPigInfoDto pigDto = pigs.get(j);
                        //根据用户拥有的猪舍权限过滤拥有user
                        List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                        // 母猪的updatedAt与当前时间差 (天)
                        DoctorPigEvent doctorPigEvent = getMatingPigEvent(pigDto);
                        Double timeDiff = getTimeDiff(new DateTime(doctorPigEvent.getEventAt()));
                        ruleValueMap.values().forEach(ruleValue -> {
                            if (checkRuleValue(ruleValue, timeDiff)) {
                                if (!isMessage && Objects.equals(ruleTemplate.getType(), DoctorMessageRuleTemplate.Type.WARNING.getValue())) {
                                    // 获取预产期, 并校验日期
                                    //DateTime birthDate = getBirthDate(pigDto, ruleValue);
                                    // 记录每只猪的消息提醒
                                    recordPigMessage(pigDto, PigEvent.FARROWING, getRuleTimeDiff(ruleValue, timeDiff), ruleValue,
                                            PigStatus.Pregnancy);
                                }
                                if (isMessage) {
                                    pigDto.setEventDate(doctorPigEvent.getEventAt());
                                    pigDto.setOperatorName(doctorPigEvent.getOperatorName());
                                    getMessage(pigDto, ruleRole, sUsers, timeDiff, rule.getUrl(), PigEvent.FARROWING.getKey(), ruleValue.getId());
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("[SowBirthDateProduce]-handle.message.failed");
                    }
                }
            }
        }
    }
}
