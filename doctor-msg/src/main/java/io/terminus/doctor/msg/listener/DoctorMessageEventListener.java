package io.terminus.doctor.msg.listener;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageUserWriteService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by xiao on 16/9/21.
 */
@Component
@Slf4j
public class DoctorMessageEventListener implements EventListener{
    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    private ZKClientFactory zkClientFactory;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired
    private DoctorMessageWriteService doctorMessageWriteService;

    @Autowired
    private DoctorMessageReadService doctorMessageReadService;

    @Autowired
    private DoctorMessageUserWriteService doctorMessageUserWriteService;

    @PostConstruct
    public void subs() {
        try{
            log.info("zk{}", zkClientFactory);
            if (subscriber == null) {
                return;
            }
            subscriber.subscribe(data -> {
                DataEvent dataEvent = DataEvent.fromBytes(data);
                if (dataEvent != null && dataEvent.getEventType() != null) {
                    coreEventDispatcher.publish(dataEvent);
                }
            });
        } catch (Exception e) {
            log.error("subscriber failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    @Subscribe
    public void handleEvent(DataEvent dataEvent){
        log.info("data event data:{}", dataEvent);
        // 1. 如果是猪创建事件信息
        if (DataEventType.PigEventCreate.getKey() == dataEvent.getEventType()) {
            PigCreateEvent pigCreateEvent = DataEvent.analyseContent(dataEvent, PigCreateEvent.class);
            if (pigCreateEvent != null && pigCreateEvent.getContext() != null) {
                Map<String, Object> context = pigCreateEvent.getContext();
                handlePigEvent(context);
            }
        }
//
//        // 2. 如果是猪群信息修改
//        if (DataEventType.GroupEventCreate.getKey() == dataEvent.getEventType()) {
//            Map<String, Serializable> context = DataEvent.analyseContent(dataEvent, Map.class);
//            Long groupId = Params.getWithConvert(context, "doctorGroupId", d -> Long.valueOf(d.toString()));
//        }
    }

    private void handlePigEvent(Map<String, Object> context){
        if("single".equals(context.get("contextType"))) {
            Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
            Integer eventType = Params.getWithConvert(context, "type", d -> Integer.valueOf(d.toString()));
            updateMessage(pigId, eventType);
        }else {
            context.remove("contextType");
            context.values().forEach(inContext -> {
                if (inContext != null) {
                    Map inContextMap = (Map) inContext;
                    Long pigId = Params.getWithConvert(inContextMap, "doctorPigId", d -> Long.valueOf(d.toString()));
                    Integer eventType = Params.getWithConvert(inContextMap, "type", d -> Integer.valueOf(d.toString()));
                    updateMessage(pigId, eventType);
                }
            });
        }
    }

    /**
     * 当猪触发事件之后, 清除event类型的消息数据.
     * @param businessId
     * @param eventType
     */
    private void updateMessage(Long businessId, Integer eventType) {

        for (int i = 0; ; i++) {
            DoctorMessageSearchDto doctorMessageSearchDto = new DoctorMessageSearchDto();
            doctorMessageSearchDto.setBusinessId(businessId);
            doctorMessageSearchDto.setEventType(eventType);
            List<DoctorMessage> messages = RespHelper.or500(doctorMessageReadService.findMessageListByCriteria(doctorMessageSearchDto));
            if (messages != null && messages.size() > 0) {
                messages.forEach(doctorMessage -> {
                    doctorMessageWriteService.deleteMessageById(doctorMessage.getId());
                    doctorMessageUserWriteService.deleteByMessageId(doctorMessage.getId());
                });
            }
        }

    }
}
