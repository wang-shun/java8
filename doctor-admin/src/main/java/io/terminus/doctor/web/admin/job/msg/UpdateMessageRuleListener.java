package io.terminus.doctor.web.admin.job.msg;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorMessageRule;
import io.terminus.doctor.event.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.event.service.DoctorMessageRuleReadService;
import io.terminus.doctor.event.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.web.admin.job.msg.producer.AbstractJobProducer;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by xjn on 16/11/15.
 */
@Slf4j
@Component
public class UpdateMessageRuleListener implements EventListener {

    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    private DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;
    @Autowired
    private DoctorMessageRuleReadService doctorMessageRuleReadService;
    @Autowired
    private ApplicationContext applicationContext;
    private Map<String, AbstractJobProducer> producerMap;

    @PostConstruct
    public void subs() {
        try{
            producerMap = applicationContext.getBeansOfType(AbstractJobProducer.class);
            if (producerMap == null) {
                producerMap = Maps.newHashMap();
            }

            if (subscriber == null) {
                return;
            }
            subscriber.subscribe(data -> {
                DataEvent dataEvent = DataEvent.fromBytes(data);
                if (dataEvent != null && dataEvent.getEventType() != null) {
                    handleUpdateMessageRule(dataEvent);
                }
            });
        } catch (Exception e) {
            log.error("subscriber failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    private void handleUpdateMessageRule(DataEvent dataEvent){
        log.info("data event data:{}", dataEvent);
        try {
            if (Objects.equals(DataEventType.UpdateMessageRule.getKey(), dataEvent.getEventType())) {
                Map<String, Integer> map = DataEvent.analyseContent(dataEvent, Map.class);
                DoctorMessageRule messageRule = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRuleById(map.get("messageRuleId").longValue()));
                createWarnMessage(messageRule);
            } else if (Objects.equals(DataEventType.UpdateMessageRules.getKey(), dataEvent.getEventType())){
                Map<String, List<Integer>> map = DataEvent.analyseContent(dataEvent, Map.class);
                Map<Integer, DoctorMessageRule> ruleMap = Maps.newHashMap();
                map.get("messageRuleIds").stream()
                        .forEach(messageRuleId -> {
                            DoctorMessageRule messageRule = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRuleById(messageRuleId.longValue()));
                            ruleMap.put(messageRule.getCategory(), messageRule);
                        });
                ruleMap.values().forEach(this::createWarnMessage);
            }
        } catch (Exception e) {
            log.error("handle.update.message.rule.failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    private void createWarnMessage(DoctorMessageRule messageRule){
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("farmId", messageRule.getFarmId());
        criteria.put("category", messageRule.getCategory());
        List<DoctorMessageRule> ruleList = RespHelper.orServEx(doctorMessageRuleReadService.findMessageRulesByCriteria(criteria));
        ruleList.forEach(rule -> {
            DoctorMessageRuleTemplate doctorMessageRuleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(messageRule.getTemplateId()));
            if (producerMap.get(doctorMessageRuleTemplate.getProducer()) != null){
                producerMap.get(doctorMessageRuleTemplate.getProducer()).createWarnMessageByMessageRule(rule);
            }
        });
    }
}
