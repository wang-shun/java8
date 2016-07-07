package io.terminus.doctor.schedule.msg.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
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
import io.terminus.doctor.schedule.msg.producer.factory.MaterialDtoFactory;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeAvgDto;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeAvgReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Desc: 仓库库存不足提示
 *          1. 物料剩余天数
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class StorageShortageProducer extends AbstractJobProducer {

    private final DoctorMaterialConsumeAvgReadService doctorMaterialConsumeAvgReadService;

    @Autowired
    public StorageShortageProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                                   DoctorMessageRuleReadService doctorMessageRuleReadService,
                                   DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                                   DoctorMessageReadService doctorMessageReadService,
                                   DoctorMessageWriteService doctorMessageWriteService,
                                   DoctorMaterialConsumeAvgReadService doctorMaterialConsumeAvgReadService,
                                   DoctorMessageTemplateReadService doctorMessageTemplateReadService,
                                   DoctorPigReadService doctorPigReadService,
                                   DoctorPigWriteService doctorPigWriteService,
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
                Category.STORAGE_SHORTAGE);
        this.doctorMaterialConsumeAvgReadService = doctorMaterialConsumeAvgReadService;
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("仓库库存不足消息产生 --- PigVaccinationProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        // 获取farmid下所有跟踪的仓库和物料数据
        List<DoctorMaterialConsumeAvgDto> materialConsumeAvgs = RespHelper.orServEx(doctorMaterialConsumeAvgReadService.findMaterialConsumeAvgsByFarmId(ruleRole.getFarmId()));
        for (int i = 0; materialConsumeAvgs != null && i < materialConsumeAvgs.size(); i++) {
            DoctorMaterialConsumeAvgDto materialConsumeAvg = materialConsumeAvgs.get(i);
            Integer lotConsumeDay = materialConsumeAvg.getLotConsumeDay();
            if (ruleValueMap.get(1) != null && lotConsumeDay != null) {
                // 如果剩余使用天数 小于 配置的天数
                if (lotConsumeDay < ruleValueMap.get(1).getValue()) {
                    messages.addAll(getMessage(materialConsumeAvg, rule.getChannels(), ruleRole, subUsers, rule.getUrl()));
                }
            }
        }

        log.info("仓库库存不足消息产生 --- PigVaccinationProducer 结束执行, 产生 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 创建消息
     */
    private List<DoctorMessage> getMessage(DoctorMaterialConsumeAvgDto materialConsumeAvg, String channels, DoctorMessageRuleRole ruleRole, List<SubUser> subUsers, String url) {
        List<DoctorMessage> messages = Lists.newArrayList();
        // 创建消息
        Map<String, Object> jsonData = MaterialDtoFactory.getInstance().createMaterialMessage(materialConsumeAvg, url);

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
