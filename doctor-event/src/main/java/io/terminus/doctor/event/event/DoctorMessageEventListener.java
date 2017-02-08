package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.msg.DoctorMessageSearchDto;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.service.DoctorMessageReadService;
import io.terminus.doctor.event.service.DoctorMessageUserWriteService;
import io.terminus.doctor.event.service.DoctorMessageWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xjn on 16/9/21.
 * 消息监听
 */
@Component
@Slf4j
public class DoctorMessageEventListener implements EventListener{
    @Autowired
    private DoctorMessageWriteService doctorMessageWriteService;

    @Autowired
    private DoctorMessageReadService doctorMessageReadService;

    @Autowired
    private DoctorMessageUserWriteService doctorMessageUserWriteService;

    @Subscribe
    public void handlePigEvent(MsgListenedPigEvent listenedPigEvent) {
        log.info("data event data:{}", listenedPigEvent);
        // 猪创建事件信息
        if (listenedPigEvent != null && !Arguments.isNullOrEmpty(listenedPigEvent.getPigs())) {
            listenedPigEvent.getPigs().forEach(pigPublishDto -> updateMessage(pigPublishDto.getPigId(), pigPublishDto.getEventType(), DoctorMessage.BUSINESS_TYPE.PIG.getValue()));
        }
    }

    public void handleGroupEvent(MsgListenedGroupEvent listenedGroupEvent) {
        log.info("data event data:{}", listenedGroupEvent);
        // 猪群信息修改
        if (listenedGroupEvent != null && !Arguments.isNullOrEmpty(listenedGroupEvent.getGroups())) {
            listenedGroupEvent.getGroups().forEach(groupPublishDto -> updateMessage(groupPublishDto.getGroupId(), groupPublishDto.getEventType(), DoctorMessage.BUSINESS_TYPE.GROUP.getValue()));
        }
    }
    /**
     * 当触发事件之后, 清除event类型的消息数据.
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
