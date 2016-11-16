package io.terminus.doctor.schedule.msg;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.producer.AbstractProducer;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
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
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @Autowired
    private PrimaryUserReadService primaryUserReadService;
    @Autowired
    private ApplicationContext applicationContext;
    private Map<String, AbstractProducer> producerMap;

    @PostConstruct
    public void subs() {
        try{
            producerMap = applicationContext.getBeansOfType(AbstractProducer.class);
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
        if (Objects.equals(DataEventType.UpdateMessageRule.getKey(), dataEvent.getEventType())) {
            try {
                DoctorMessageRule messageRule = DataEvent.analyseContent(dataEvent, DoctorMessageRule.class);
                DoctorMessageRuleTemplate doctorMessageRuleTemplate = RespHelper.orServEx(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(messageRule.getTemplateId()));
                if (producerMap.get(doctorMessageRuleTemplate.getProducer()) != null){
                    producerMap.get(doctorMessageRuleTemplate.getProducer()).createWarnMessageByMessageRule(messageRule, getUsersHasFarm(messageRule.getFarmId()));
                }
            } catch (Exception e) {
               log.error("handle.update.message.rule.failed, cause by {}", Throwables.getStackTraceAsString(e));
            }

        }
    }

    private List<SubUser> getUsersHasFarm(Long farmId){
        List<SubUser> subUsers = Lists.newArrayList();
        List<Long> userIds = Lists.newArrayList();
        List<Sub> subs = RespHelper.orServEx(primaryUserReadService.findAllActiveSubs());
        if (!Arguments.isNullOrEmpty(subs)){
            userIds.addAll(subs.stream().map(sub -> sub.getUserId()).collect(Collectors.toList()));
        }
        List<PrimaryUser> primaryUsers = RespHelper.orServEx(primaryUserReadService.findAllPrimaryUser());
        if (!Arguments.isNullOrEmpty(primaryUsers)){
            userIds.addAll(primaryUsers.stream().map(sub -> sub.getUserId()).collect(Collectors.toList()));
        }
        userIds.forEach(userId -> {
            SubUser subUser = SubUser.builder()
                    .userId(userId)
                    .farmIds(Lists.newArrayList())
                    .barnIds(Lists.newArrayList())
                    .build();
            // 获取猪场权限
            DoctorUserDataPermission dataPermission = RespHelper.orServEx(
                    doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (dataPermission != null) {
                dataPermission.setFarmIds(dataPermission.getFarmIds());
                if (dataPermission.getFarmIdsList().contains(farmId)){
                    dataPermission.setBarnIds(dataPermission.getBarnIds());
                    subUser.getBarnIds().addAll(dataPermission.getBarnIdsList());
                    subUsers.add(subUser);
                }
            }

        });
        return subUsers;
    }
}
