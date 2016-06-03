package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.dto.RuleValue;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import io.terminus.doctor.msg.producer.AbstractProducer;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.schedule.msg.producer.factory.PigDtoFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 母猪需妊娠检查提示
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/3
 */
@Component
@Slf4j
public class SowPregCheckProducer extends AbstractProducer {


    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public SowPregCheckProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                               DoctorMessageRuleReadService doctorMessageRuleReadService,
                               DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                               DoctorMessageReadService doctorMessageReadService,
                               DoctorMessageWriteService doctorMessageWriteService,
                               DoctorPigReadService doctorPigReadService) {
        super(doctorMessageRuleTemplateReadService,
                doctorMessageRuleReadService,
                doctorMessageRuleRoleReadService,
                doctorMessageReadService,
                doctorMessageWriteService,
                Category.SOW_PREGCHECK);
        this.doctorPigReadService = doctorPigReadService;
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {

        log.info("母猪需妊娠提示消息产生 --- SowPregCheckProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        // 批量获取猪信息
        Long total = RespHelper.orServEx(doctorPigReadService.queryPigCount(
                DataRange.FARM.getKey(), ruleRole.getFarmId(), DoctorPig.PIG_TYPE.SOW.getKey()));
        Rule rule = ruleRole.getRule();

        if (StringUtils.isNotBlank(rule.getChannels())) {
            // 计算size, 分批处理
            Long page = getPageSize(total, 100L);
            DoctorPig pig = DoctorPig.builder()
                    .farmId(ruleRole.getFarmId())
                    .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                    .build();
            for (int i = 1; i <= page; i++) {
                List<DoctorPigInfoDto> pigs = RespHelper.orServEx(doctorPigReadService.pagingDoctorInfoDtoByPig(pig, i, 100)).getData();
                for (int j = 0; pigs != null && j < pigs.size(); j++) {
                    DoctorPigInfoDto pigDto = pigs.get(j);
                    // 母猪的updatedAt与当前时间差 (天)
                    Double timeDiff = (double) (DateTime.now().minus(new DateTime(pigDto.getUpdatedAt()).getMillis()).getMillis() / 86400000);
                    for (int k = 0; rule.getValues() != null && k < rule.getValues().size(); k++) {
                        RuleValue ruleValue = rule.getValues().get(k);
                        // 1. 妊娠检查判断 -> id:1
                        if (1 == ruleValue.getId() && Objects.equals(PigStatus.Mate.getKey(), pigDto.getStatus())) {
                            if (checkRuleValue(ruleValue, timeDiff)) {
                                messages.addAll(getMessage(pigDto, rule.getChannels(), ruleRole, subUsers, timeDiff, rule.getUrl()));
                            }
                        }
                    }
                }
            }
        }
        log.info("母猪需妊娠提示消息产生 --- SowPregCheckProducer 结束执行, 产生 {} 条消息", messages.size());

        return messages;
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
