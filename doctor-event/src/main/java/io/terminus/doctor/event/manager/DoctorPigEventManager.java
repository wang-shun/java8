package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.ZkGroupPublishDto;
import io.terminus.doctor.common.event.ZkListenedGroupEvent;
import io.terminus.doctor.common.event.ZkListenedPigEvent;
import io.terminus.doctor.common.event.ZkPigPublishDto;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
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
import io.terminus.doctor.event.handler.DoctorPigsByEventSelector;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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

    /**
     * 事件处理
     * @param inputDto 事件信息数据
     * @param basic 基础数据
     */
    @Transactional
    public List<DoctorEventInfo> eventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        DoctorPigEventHandler doctorEventCreateHandler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
        doctorEventCreateHandler.handleCheck(inputDto, basic);
        final List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
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
        //校验输入数据的重复性
        eventRepeatCheck(eventInputs);

        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(basic.getEventType());
        final List<DoctorEventInfo> eventInfos = Lists.newArrayList();
        eventInputs.forEach(inputDto -> {
            handler.handleCheck(inputDto, basic);
            handler.handle(eventInfos, inputDto, basic);
        });
        return eventInfos;
    }

    /**
     * 校验携带数据正确性，发布事件
     */
    public static void  checkAndPublishEvent(List<DoctorEventInfo> dtos, CoreEventDispatcher coreEventDispatcher, Publisher publisher) {
        try {
            if (notEmpty(dtos)) {
                //checkFarmIdAndEventAt(dtos);
                publishPigEvent(dtos, coreEventDispatcher, publisher);
            }
        } catch (Exception e) {
            log.error("publish event failed, dtos:{}, cause: {}", dtos, Throwables.getStackTraceAsString(e));
        }
    }

    //发布事件, 用于更新创建操作
    private static void publishPigEvent(List<DoctorEventInfo> eventInfoList, CoreEventDispatcher coreEventDispatcher, Publisher publisher) {

        if (Arguments.isNullOrEmpty(eventInfoList)) {
            return;
        }
        Long orgId = eventInfoList.get(0).getOrgId();
        Long farmId = eventInfoList.get(0).getFarmId();

        Map<Integer, List<DoctorEventInfo>> eventInfoMap = eventInfoList.stream()
                .collect(Collectors.groupingBy(DoctorEventInfo::getBusinessType));

        //1.发布猪事件
        List<DoctorEventInfo> pigEventList = eventInfoMap.get(DoctorEventInfo.Business_Type.PIG.getValue());
        if (!Arguments.isNullOrEmpty(pigEventList)) {
           publishPigEvent(pigEventList, orgId, farmId, coreEventDispatcher, publisher);
        }

        //2.发布猪群事件
        List<DoctorEventInfo> groupEventList = eventInfoMap.get(DoctorEventInfo.Business_Type.GROUP.getValue());
        if (!Arguments.isNullOrEmpty(groupEventList)) {
            publishGroupEvent(groupEventList, orgId, farmId, coreEventDispatcher, publisher);
        }

    }

    /**
     * 发布猪事件
     * @param eventInfoList 猪事件列表
     * @param orgId 公司id
     * @param farmId 猪场id
     */
    private static void publishPigEvent(List<DoctorEventInfo> eventInfoList, Long orgId, Long farmId, CoreEventDispatcher coreEventDispatcher, Publisher publisher){
        //猪事件触发报表更新(eventBus)
        Map<Integer, List<DoctorEventInfo>> pigEventInfoMap = eventInfoList.stream()
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
            coreEventDispatcher.publish(new ListenedPigEvent(orgId, farmId, eventType, pigPublishDtoList));
        });
        //猪事件触发更新消息(zk)
        try {
            List<ZkPigPublishDto> zkPigPublishDtoList = eventInfoList.stream()
                    .filter(doctorEventInfo -> PigEvent.NOTICE_MESSAGE_PIG_EVENT.contains(doctorEventInfo.getEventType()))
                    .map(doctorEventInfo -> {
                        return ZkPigPublishDto.builder()
                                .pigId(doctorEventInfo.getBusinessId())
                                .eventAt(doctorEventInfo.getEventAt())
                                .eventId(doctorEventInfo.getEventId())
                                .eventType(doctorEventInfo.getEventType())
                                .build();
                    }).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(zkPigPublishDtoList)) {
                publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new ZkListenedPigEvent(orgId, farmId, zkPigPublishDtoList)));
            }
        } catch (Exception e) {
            log.error("publish.pig.event.fail");
        }
    }

    /**
     * 发布猪群事件
     * @param eventInfoList 猪群事件列表
     * @param orgId 公司id
     * @param farmId 猪场id
     */
    private static void publishGroupEvent(List<DoctorEventInfo> eventInfoList, Long orgId, Long farmId, CoreEventDispatcher coreEventDispatcher, Publisher publisher) {
        //猪群事件触发报表更新(eventBus)
        Map<Integer, List<DoctorEventInfo>> groupEventInfoMap = eventInfoList.stream()
                .collect(Collectors.groupingBy(DoctorEventInfo::getEventType));
        groupEventInfoMap.keySet().forEach(eventType -> {
            List<DoctorGroupPublishDto> groupPublishDtoList = groupEventInfoMap.get(eventType).stream().map(doctorEventInfo -> {
                DoctorGroupPublishDto groupPublishDto = new DoctorGroupPublishDto();
                groupPublishDto.setGroupId(doctorEventInfo.getBusinessId());
                groupPublishDto.setEventId(doctorEventInfo.getEventId());
                groupPublishDto.setEventAt(doctorEventInfo.getEventAt());
                groupPublishDto.setPigType(doctorEventInfo.getPigType());
                return groupPublishDto;
            }).collect(Collectors.toList());
            coreEventDispatcher.publish(new ListenedGroupEvent(orgId, farmId, eventType, groupPublishDtoList));
        });
        //猪群事件触发的消息更新(zk)
        try {
            List<ZkGroupPublishDto> zkGroupPublishDtoList = eventInfoList.stream()
                    .filter(doctorEventInfo -> GroupEventType.NOTICE_MESSAGE_GROUP_EVENT.contains(doctorEventInfo.getEventType()))
                    .map(doctorEventInfo -> {
                        return ZkGroupPublishDto.builder()
                                .groupId(doctorEventInfo.getBusinessId())
                                .eventAt(doctorEventInfo.getEventAt())
                                .eventId(doctorEventInfo.getEventId())
                                .eventType(doctorEventInfo.getEventType())
                                .build();
                    }).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(zkGroupPublishDtoList)) {
                publisher.publish(DataEvent.toBytes(DataEventType.GroupEventClose.getKey(), new ZkListenedGroupEvent(orgId, farmId, zkGroupPublishDtoList)));
            }
        } catch (Exception e) {
            log.error("publish.pig.event.fail");
        }
    }

    private static void checkFarmIdAndEventAt(List<DoctorEventInfo> dtos) {
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

    /**
     * 可执行此事件的猪查询条件
     * @param eventType 事件类型
     * @return 查询track的条件
     */
    public DoctorSuggestPigSearch selectPigs(Integer eventType) {
        return DoctorPigsByEventSelector.select(eventType);
    }

    /**
     * 批量事件的重复性校验
     * @param inputList 批量事件输入
     */
    private void eventRepeatCheck(List<BasePigEventInputDto> inputList) {
        Set<String> inputSet = inputList.stream().map(BasePigEventInputDto::getPigCode).collect(Collectors.toSet());
        if (inputList.size() != inputSet.size()) {
            throw new ServiceException("batch.event.pigCode.not.repeat");
        }
    }
}
