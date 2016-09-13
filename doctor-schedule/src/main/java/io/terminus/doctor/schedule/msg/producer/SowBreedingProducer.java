package io.terminus.doctor.schedule.msg.producer;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorBarnReadService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 待配种母猪提示
 * <p>
 * 1. 断奶/流产/返情日期
 * <p>
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Component
@Slf4j
public class SowBreedingProducer extends AbstractJobProducer {

    private DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public SowBreedingProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                               DoctorMessageRuleReadService doctorMessageRuleReadService,
                               DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                               DoctorMessageReadService doctorMessageReadService,
                               DoctorMessageWriteService doctorMessageWriteService,
                               DoctorPigReadService doctorPigReadService,
                               DoctorPigWriteService doctorPigWriteService,
                               DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                               DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                               DoctorBarnReadService doctorBarnReadService) {
        super(doctorMessageTemplateReadService,
                doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                doctorPigReadService,
                doctorPigWriteService,
                doctorUserDataPermissionReadService,
                Category.SOW_BREEDING);
        this.doctorBarnReadService = doctorBarnReadService;
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("待配种母猪提示消息产生 --- SowBreedingProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();
        handleMessages(ruleRole.getRule(), ruleRole.getTemplateId(), ruleRole.getFarmId(), true, messages, ruleRole, subUsers);
        log.info("待配种母猪提示消息产生 --- SowBreedingProducer 结束执行, 产生 {} 条消息", messages.size());
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
                // 过滤出 断奶/流产/空怀 的母猪
                pigs = pigs.stream().filter(pigDto ->
                        Objects.equals(PigStatus.Wean.getKey(), pigDto.getStatus())
                                || Objects.equals(PigStatus.KongHuai.getKey(), pigDto.getStatus())
                                || Objects.equals(PigStatus.Entry.getKey(), pigDto.getStatus())
                ).collect(Collectors.toList());
                // 处理每个猪
                for (int j = 0; pigs != null && j < pigs.size(); j++) {
                    try {
                        DoctorPigInfoDto pigDto = pigs.get(j);
                        //根据用户拥有的猪舍权限过滤拥有user
                        List<SubUser> sUsers = filterSubUserBarnId(subUsers, pigDto.getBarnId());
                        // 母猪的updatedAt与当前时间差 (天)
                        if (getStatusDate(pigDto) != null) {
                            Double timeDiff = getTimeDiff(getStatusDate(pigDto));
                            // 获取配置的天数, 并判断
                            // 记录每只猪的消息提醒
                            if (!isMessage && Objects.equals(ruleTemplate.getType(), DoctorMessageRuleTemplate.Type.WARNING.getValue())) {
                                recordPigMessage(pigDto, PigEvent.MATING, getRuleTimeDiff(ruleValueMap.get(1), timeDiff), ruleValueMap.get(1).getValue().intValue(),
                                        PigStatus.Wean, PigStatus.KongHuai, PigStatus.Entry);
                            }

                            if (isMessage && checkRuleValue(ruleValueMap.get(1), timeDiff)) {
                                pigDto.setOperatorName(RespHelper.orServEx(doctorBarnReadService.findBarnById(pigDto.getBarnId())).getStaffName());
                                messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, sUsers, timeDiff, rule.getUrl()));
                            }

                        }
                    } catch (Exception e) {
                        log.error("[SowBreedingProduce]-handle.message.failed");
                    }

                }
            }
        }
    }

}
