package io.terminus.doctor.msg.listener;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.DoctorZkGroupEvent;
import io.terminus.doctor.common.event.DoctorZkPigEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageUserWriteService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xiao on 16/9/21.
 */
@Component
@Slf4j
public class DoctorMessageEventListener implements EventListener{
    @Autowired(required = false)
    private Subscriber subscriber;

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
            DoctorZkPigEvent pigCreateEvent = DataEvent.analyseContent(dataEvent, DoctorZkPigEvent.class);
            if (pigCreateEvent != null && pigCreateEvent.getContext() != null) {
                Map<String, Object> context = pigCreateEvent.getContext();
                handlePigEvent(context);
            }
        }

        // 2. 如果是猪群信息修改
        if (DataEventType.GroupEventClose.getKey() == dataEvent.getEventType()) {
            DoctorZkGroupEvent zkGroupEvent = DataEvent.analyseContent(dataEvent, DoctorZkGroupEvent.class);
            if (zkGroupEvent != null && zkGroupEvent.getContext() != null) {
                Map<String, Object> context = zkGroupEvent.getContext();
                Long groupId = Params.getWithConvert(context, "doctorGroupId", d -> Long.valueOf(d.toString()));
                Integer eventType = Params.getWithConvert(context, "eventType", d -> Integer.valueOf(d.toString()));
                updateMessage(groupId, eventType, DoctorMessage.BUSINESS_TYPE.GROUP.getValue());
            }
        }
    }

    private void handlePigEvent(Map<String, Object> context){
        if("single".equals(context.get("contextType"))) {
            Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
            Integer eventType = Params.getWithConvert(context, "type", d -> Integer.valueOf(d.toString()));
            updateMessage(pigId, eventType, DoctorMessage.BUSINESS_TYPE.PIG.getValue());
        }else {
            context.remove("contextType");
            context.values().forEach(inContext -> {
                if (inContext != null) {
                    Map inContextMap = (Map) inContext;
                    Long pigId = Params.getWithConvert(inContextMap, "doctorPigId", d -> Long.valueOf(d.toString()));
                    Integer eventType = Params.getWithConvert(inContextMap, "type", d -> Integer.valueOf(d.toString()));
                    updateMessage(pigId, eventType, DoctorMessage.BUSINESS_TYPE.PIG.getValue());
                }
            });
        }
    }

    /**
     * 当猪触发事件之后, 清除event类型的消息数据.
     * @param businessId
     * @param eventType
     */
    private void updateMessage(Long businessId, Integer eventType, Integer businessType) {
        DoctorMessageSearchDto doctorMessageSearchDto = new DoctorMessageSearchDto();
        doctorMessageSearchDto.setBusinessId(businessId);
        doctorMessageSearchDto.setBusinessType(businessType);
        if (eventType != 6 && eventType != 10){
            doctorMessageSearchDto.setEventType(eventType);
        }
        List<Long> messageIds = RespHelper.orServEx(doctorMessageReadService.findMessageListByCriteria(doctorMessageSearchDto)).stream().map(DoctorMessage::getId).collect(Collectors.toList());;
        if (!Arguments.isNullOrEmpty(messageIds)){
            doctorMessageWriteService.deleteMessagesByIds(messageIds);
            doctorMessageUserWriteService.deletesByMessageIds(messageIds);
        }
    }
}
