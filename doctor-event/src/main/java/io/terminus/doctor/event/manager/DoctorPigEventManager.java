package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.EventRequestStatus;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.event.DoctorGroupPublishDto;
import io.terminus.doctor.event.event.DoctorPigPublishDto;
import io.terminus.doctor.event.event.ListenedGroupEvent;
import io.terminus.doctor.event.event.ListenedPigEvent;
import io.terminus.doctor.event.event.MsgGroupPublishDto;
import io.terminus.doctor.event.event.MsgListenedGroupEvent;
import io.terminus.doctor.event.event.MsgListenedPigEvent;
import io.terminus.doctor.event.event.MsgPigPublishDto;
import io.terminus.doctor.event.handler.DoctorEventSelector;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import io.terminus.doctor.event.handler.DoctorPigsByEventSelector;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorEventModifyRequestDao eventModifyRequestDao;

    private static final List<Integer> NOT_MODIFY_EVENT = Lists.newArrayList(PigEvent.CHG_LOCATION.getKey(), PigEvent.CHG_FARM.getKey(), PigEvent.FOSTERS.getKey(), PigEvent.FOSTERS_BY.getKey());

    private static final List<Integer> TRIGGER_GROUP_EVENT = Lists.newArrayList(PigEvent.CHG_LOCATION.getKey(), PigEvent.CHG_FARM.getKey(),
            PigEvent.FOSTERS.getKey(), PigEvent.FOSTERS_BY.getKey(),
            PigEvent.FARROWING.getKey(), PigEvent.PIGLETS_CHG.getKey());

    /**
     * 事件处理
     * @param inputDto 事件信息数据
     * @param basic 基础数据
     */
    @Transactional
    public List<DoctorEventInfo> eventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        log.info("pig event handle starting, inputDto:{}, basic:{}", inputDto, basic);

        final List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(inputDto.getEventType());
        //获取需要执行的事件
        DoctorPigEvent executeEvent = handler.buildPigEvent(basic, inputDto);
        //事件执行前的状态
        DoctorPigTrack fromTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        //数据校验
        handler.handleCheck(executeEvent, fromTrack);
        //处理事件
        handler.handle(doctorEventInfoList, executeEvent, fromTrack);

        log.info("pig event handle ending, inputDto:{}, basic:{}", inputDto, basic);
        return doctorEventInfoList;
    }

    public void modifyPigEventRequestHandle(DoctorEventModifyRequest modifyRequest) {
        log.info("modify pig event handle starting, modifyRequest:{}", modifyRequest);
        try {
            modifyRequest.setStatus(EventRequestStatus.HANDLING.getValue());
            eventModifyRequestDao.update(modifyRequest);

            //处理猪事件修改
            DoctorPigEvent modifyEvent = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(modifyRequest.getContent(), DoctorPigEvent.class);
            modifyPigEventHandle(modifyEvent);

            //更新修改请求的状态
            modifyRequest.setStatus(EventRequestStatus.SUCCESS.getValue());
        } catch (Exception e) {
            log.info("modify pig event request handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            modifyRequest.setStatus(EventRequestStatus.FAILED.getValue());
            modifyRequest.setReason("");
        }
        eventModifyRequestDao.update(modifyRequest);
        log.info("modify pig event handle ending");
    }
    /**
     * 事件编辑处理
     * @param modifyEvent 编辑事件
     */
    public void modifyPigEventHandle(DoctorPigEvent modifyEvent) {
        //事件能否编辑初步校验
        canModify(modifyEvent);

        List<DoctorEventInfo> doctorEventInfoList = Lists.newArrayList();
        List<Long> pigOldEventIdList = Lists.newLinkedList();
        DoctorPigTrack currentTrack = doctorPigTrackDao.findByPigId(modifyEvent.getPigId());
        try {
            //1.处理猪事件编辑
            modifyPigEventHandle(modifyEvent, doctorEventInfoList, pigOldEventIdList);

            //2.处理关联的猪群事件编辑
            if (TRIGGER_GROUP_EVENT.contains(modifyEvent.getType())) {
                //获取猪群需要修改原事件
                DoctorGroupEvent oldGroupModifyEvent = doctorGroupEventDao.findByRelPigEventId(modifyEvent.getId());

                //获取猪群修改事件前track

                //构建新猪群事件

                //猪群事件编辑
            }
        } catch (Exception e) {
            //回滚猪事件编辑
            modifyPidEventRollback(doctorEventInfoList.stream()
                    .filter(doctorEventInfo -> Objects.equals(doctorEventInfo.getBusinessType(), DoctorEventInfo.Business_Type.PIG.getValue()))
                    .map(DoctorEventInfo::getEventId).collect(Collectors.toList()), pigOldEventIdList, currentTrack);
            //暂定
            log.info("modify pig event handle failed, cause by:{}", Throwables.getStackTraceAsString(e));
            throw new ServiceException("pig.event.modify.failed");
        }

    }


    /**
     * 事件能否编辑初步校验
     * @param modifyEvent 编辑事件
     * @return 能否编辑
     */
    private Boolean canModify(DoctorPigEvent modifyEvent) {
        return Objects.equals(modifyEvent.getType(), DoctorEventModifyRequest.TYPE.PIG.getValue())
                && !NOT_MODIFY_EVENT.contains(modifyEvent.getType())
                && Objects.equals(modifyEvent.getIsAuto(), IsOrNot.NO.getValue());
    }

    /**
     * 猪事件编辑具体实现
     * @param modifyEvent 编辑之后的事件
     * @param doctorEventInfoList 事件信息
     * @param oldEventIdList 原事件id列表
     */
    private void modifyPigEventHandle (DoctorPigEvent modifyEvent, List<DoctorEventInfo> doctorEventInfoList, List<Long> oldEventIdList) {
        oldEventIdList.add(modifyEvent.getId());

//        //获取修改后事件
//        DoctorPigEvent newModifyEvent = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(modifyRequest.getContent(), DoctorPigEvent.class);

        //获取修改前猪track
        DoctorPigTrack fromTrack = null;
        if (!Objects.equals(modifyEvent.getType(), PigEvent.ENTRY.getKey())) {
            DoctorPigSnapshot lastPigSnapshot = doctorPigSnapshotDao.queryByEventId(modifyEvent.getId());
            fromTrack = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.fromJson(lastPigSnapshot.getToPigInfo(), DoctorPigSnapShotInfo.class).getPigTrack();
        }
        
        //获取事件处理器
        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(modifyEvent.getType());

        //处理事件
        handler.handle(doctorEventInfoList, modifyEvent, fromTrack);

        //获取事件处理
        List<DoctorPigEvent> followEventList = doctorPigEventDao.findFollowEvents(modifyEvent.getPigId(), modifyEvent.getId());
        if (followEventList.isEmpty()) {
            return;
        }

        //将原事件状态置为无效
        oldEventIdList.addAll(followEventList.stream().map(DoctorPigEvent::getId).collect(Collectors.toList()));
        doctorPigEventDao.updateEventsStatus(oldEventIdList, EventStatus.INVALID.getValue());

        //处理后续事件
        followEventList.forEach(followEvent -> followPigEventHandle(doctorEventInfoList, followEvent));
    }

    /**
     * 处理后续猪事件
     * @param doctorEventInfoList 事件信息列表
     * @param executeEvent 后续事件
     */
    private void followPigEventHandle(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent) {
        executeEvent.setIsModify(IsOrNot.YES.getValue());
        DoctorPigTrack fromTrack = doctorPigTrackDao.findByPigId(executeEvent.getPigId());
        //获取事件处理器
        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(executeEvent.getType());
        //事件校验
        handler.handleCheck(executeEvent, fromTrack);
        //事件处理
        handler.handle(doctorEventInfoList, executeEvent, fromTrack);
    }

    /**
     * 猪事件编辑错误时回滚
     * @param pigOldEventIdList 原事件列
     * @param pigNewEventIdList 新事件列表
     * @param fromTrack 事件编辑前猪track
     */
    @Transactional
    private void modifyPidEventRollback(List<Long> pigOldEventIdList, List<Long> pigNewEventIdList, DoctorPigTrack fromTrack) {
        //1.将新生成事件置为无效
        if (Arguments.isNullOrEmpty(pigNewEventIdList)) {
            doctorPigEventDao.updateEventsStatus(pigNewEventIdList, EventStatus.INVALID.getValue());
        }
        //2.将原事件置为有效
        if (Arguments.isNullOrEmpty(pigOldEventIdList)) {
            doctorPigEventDao.updateEventsStatus(pigOldEventIdList, EventStatus.VALID.getValue());
        }
        //3.还原track
        doctorPigTrackDao.update(fromTrack);
    }

    /**
     * 批量事件处理
     * @param eventInputs
     * @param basic
     * @return
     */
    @Transactional
    public List<DoctorEventInfo> batchEventsHandle(List<BasePigEventInputDto> eventInputs, DoctorBasicInputInfoDto basic) {
        log.info("batch pig event handle starting, event type:{}", eventInputs.get(0).getEventType());
        //校验输入数据的重复性
        eventRepeatCheck(eventInputs);

        DoctorPigEventHandler handler = pigEventHandlers.getEventHandlerMap().get(eventInputs.get(0).getEventType());
        final List<DoctorEventInfo> eventInfos = Lists.newArrayList();
        eventInputs.forEach(inputDto -> {
            try {
                //获取需要执行的事件
                DoctorPigEvent executeEvent = handler.buildPigEvent(basic, inputDto);
                //事件执行前的状态
                DoctorPigTrack fromTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
                //数据校验
                handler.handleCheck(executeEvent, fromTrack);
                //处理事件
                handler.handle(eventInfos, executeEvent, fromTrack);
            } catch (InvalidException e) {
               throw new InvalidException(true, e.getError(), inputDto.getPigCode(), e.getParams());
            }
        });
        log.info("batch pig event handle ending, event type:{}", eventInputs.get(0).getEventType());
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
        log.info("publish pig event starting");
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
            List<MsgPigPublishDto> msgPigPublishDtoList = eventInfoList.stream()
                    .filter(doctorEventInfo -> PigEvent.NOTICE_MESSAGE_PIG_EVENT.contains(doctorEventInfo.getEventType()))
                    .map(doctorEventInfo -> {

                        MsgPigPublishDto msgPigPublishDto =  MsgPigPublishDto.builder()
                                .pigId(doctorEventInfo.getBusinessId())
                                .eventAt(doctorEventInfo.getEventAt())
                                .eventId(doctorEventInfo.getEventId())
                                .eventType(doctorEventInfo.getEventType())
                                .build();
                        if (Objects.equals(doctorEventInfo.getEventType(), PigEvent.PREG_CHECK.getKey()) && !Objects.equals(doctorEventInfo.getPregCheckResult(), PregCheckResult.YANG.getKey())) {
                            msgPigPublishDto.setEventType(PigEvent.CONDITION.getKey());
                        }
                        return msgPigPublishDto;
                    }).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(msgPigPublishDtoList)) {
                coreEventDispatcher.publish(new MsgListenedPigEvent(orgId, farmId, msgPigPublishDtoList));
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
        log.info("publish group event starting");
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
            List<MsgGroupPublishDto> msgGroupPublishDtoList = eventInfoList.stream()
                    .filter(doctorEventInfo -> GroupEventType.NOTICE_MESSAGE_GROUP_EVENT.contains(doctorEventInfo.getEventType()))
                    .map(doctorEventInfo -> {
                        return MsgGroupPublishDto.builder()
                                .groupId(doctorEventInfo.getBusinessId())
                                .eventAt(doctorEventInfo.getEventAt())
                                .eventId(doctorEventInfo.getEventId())
                                .eventType(doctorEventInfo.getEventType())
                                .build();
                    }).collect(Collectors.toList());
            if (!Arguments.isNullOrEmpty(msgGroupPublishDtoList)) {
                coreEventDispatcher.publish(new MsgListenedGroupEvent(orgId, farmId, msgGroupPublishDtoList));
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
