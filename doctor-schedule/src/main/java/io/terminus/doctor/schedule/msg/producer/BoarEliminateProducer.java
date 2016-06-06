package io.terminus.doctor.schedule.msg.producer;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Desc: 公猪应淘汰提示
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@Component
@Slf4j
public class BoarEliminateProducer extends AbstractProducer {

    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public BoarEliminateProducer(DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
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
                Category.BOAR_ELIMINATE);
        this.doctorPigReadService = doctorPigReadService;
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole, List<SubUser> subUsers) {
        log.info("公猪应淘汰消息产生 --- BoarEliminateProducer 开始执行");
        List<DoctorMessage> messages = Lists.newArrayList();

        Rule rule = ruleRole.getRule();
        // ruleValue map
        Map<Integer, RuleValue> ruleValueMap = Maps.newHashMap();
        for (int i = 0; rule.getValues() != null && i < rule.getValues().size(); i++) {
            RuleValue ruleValue = rule.getValues().get(i);
            ruleValueMap.put(ruleValue.getId(), ruleValue);
        }

        // TODO 母猪应淘汰规则规则



        log.info("公猪应淘汰消息产生 --- BoarEliminateProducer 结束执行, 产生 {} 条消息", messages.size());
        return messages;
    }
}
