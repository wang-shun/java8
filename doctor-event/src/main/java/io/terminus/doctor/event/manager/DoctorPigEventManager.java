package io.terminus.doctor.event.manager;

import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.event.DoctorGroupPublishDto;
import io.terminus.doctor.event.event.DoctorPigPublishDto;
import io.terminus.doctor.event.event.ListenedGroupEvent;
import io.terminus.doctor.event.event.ListenedPigEvent;
import io.terminus.doctor.event.handler.DoctorEventSelector;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.event.enums.PigEvent.NOTICE_MESSAGE_PIG_EVENT;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    @Autowired
    private DoctorPigEventHandlers pigEventHandlers;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired
    private Publisher publisher;

    /**
     * 事件处理
     * @param inputDto 事件信息数据
     * @param basic 基础数据
     */
    @Transactional
    public List<DoctorEventInfo> eventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        DoctorPigEventHandler doctorEventCreateHandler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
        doctorEventCreateHandler.handleCheck(inputDto, basic);
        List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
        doctorEventCreateHandler.handle(doctorEventInfoList, inputDto, basic);
        return doctorEventInfoList;
    }

    /**
     * 批量事件处理
     * @param eventInputs
     * @param basic
     * @return
     */
    @Transactional
    public List<DoctorEventInfo> batchEventsHandle(List<BasePigEventInputDto> eventInputs, DoctorBasicInputInfoDto basic) {
        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
        List<DoctorEventInfo> eventInfos = Lists.newArrayList();
        eventInputs.forEach(inputDto -> {
            handler.handleCheck(inputDto, basic);
            handler.handle(eventInfos, inputDto, basic);
        });
        return eventInfos;
    }

    /**
     * 校验携带数据正确性，发布事件
     */
    public void  checkAndPublishEvent(List<DoctorEventInfo> dtos) {
        if (notEmpty(dtos)) {
            checkFarmIdAndEventAt(dtos);
            publishPigEvent(dtos);
        }
    }

    //发布事件, 用于更新回滚后操作
    private void publishPigEvent(List<DoctorEventInfo> eventInfoList) {

        if (Arguments.isNullOrEmpty(eventInfoList)) {
            return;
        }
        Long orgId = eventInfoList.get(0).getOrgId();
        Long farmId = eventInfoList.get(0).getFarmId();
        List<DoctorPigPublishDto> pigMessagePublishList = Lists.newArrayList();
        List<DoctorGroupPublishDto> groupMessagePublishList = Lists.newArrayList();

        Map<Integer, List<DoctorEventInfo>> eventInfoMap = eventInfoList.stream().collect(Collectors.groupingBy(DoctorEventInfo::getBusinessType));
        if (!eventInfoMap.containsKey(DoctorEventInfo.Business_Type.PIG.getValue())) {
            return;
        }
        Map<Integer, List<DoctorEventInfo>> pigEventInfoMap = eventInfoMap.get(DoctorEventInfo.Business_Type.PIG.getValue()).stream()
                .collect(Collectors.groupingBy(DoctorEventInfo::getEventType));
        pigEventInfoMap.keySet().forEach(eventType -> {
            List<DoctorPigPublishDto> pigPublishDtoList = pigEventInfoMap.get(eventType).stream().map(doctorEventInfo -> {
                DoctorPigPublishDto pigPublishDto = new DoctorPigPublishDto();
                pigPublishDto.setPigId(doctorEventInfo.getBusinessId());
                pigPublishDto.setEventId(doctorEventInfo.getEventId());
                pigPublishDto.setEventAt(doctorEventInfo.getEventAt());
                pigPublishDto.setKind(doctorEventInfo.getKind());
                pigPublishDto.setMateType(doctorEventInfo.getMateType());
                pigPublishDto.setPregCheckResult(doctorEventInfo.getPregCheckResult());
                return pigPublishDto;
            }).collect(Collectors.toList());
            if (NOTICE_MESSAGE_PIG_EVENT.contains(eventType)) {
                pigMessagePublishList.addAll(pigPublishDtoList);
            }
            coreEventDispatcher.publish(new ListenedPigEvent(orgId, farmId, eventType, pigPublishDtoList));
        });
        if (!eventInfoMap.containsKey(DoctorEventInfo.Business_Type.GROUP.getValue())) {
            return;
        }
        Map<Integer, List<DoctorEventInfo>> groupEventInfoMap = eventInfoMap.get(DoctorEventInfo.Business_Type.GROUP.getValue()).stream()
                .collect(Collectors.groupingBy(DoctorEventInfo::getEventType));
        groupEventInfoMap.keySet().forEach(eventType -> {
            List<DoctorGroupPublishDto> groupPublishDtoList = groupEventInfoMap.get(eventType).stream().map(doctorEventInfo -> {
                DoctorGroupPublishDto groupPublishDto = new DoctorGroupPublishDto();
                groupPublishDto.setGroupId(doctorEventInfo.getBusinessId());
                groupPublishDto.setEventId(doctorEventInfo.getEventId());
                groupPublishDto.setEventAt(doctorEventInfo.getEventAt());
                return groupPublishDto;
            }).collect(Collectors.toList());
            if (GroupEventType.NOTICE_MESSAGE_GROUP_EVENT.contains(eventType)) {
                groupMessagePublishList.addAll(groupPublishDtoList);
            }
            coreEventDispatcher.publish(new ListenedGroupEvent(orgId, farmId, eventType, groupPublishDtoList));
        });
//        try {
//            publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new ListenedPigEvent(orgId, farmId, pigMessagePublishList)));
//            publisher.publish(DataEvent.toBytes(DataEventType.GroupEventClose.getKey(), new ListenedGroupEvent(orgId, farmId, groupMessagePublishList)));
//
//        } catch (Exception e) {
//            log.error("publish.info.error");
//        }
    }

    private void checkFarmIdAndEventAt(List<DoctorEventInfo> dtos) {
        dtos.forEach(dto -> {
            if (dto.getFarmId() == null || dto.getEventAt() == null) {
                throw new ServiceException("publish.create.event.not.null");
            }
        });
    }

    /**
     * 猪当前可执行事件
     * @param pigStatus 猪状态
     * @param pigType 猪舍类型
     * @return 可执行事件
     */
    public List<PigEvent> selectEvents(PigStatus pigStatus, PigType pigType) {
        return DoctorEventSelector.selectPigEvent(pigStatus, pigType);
    }

}
