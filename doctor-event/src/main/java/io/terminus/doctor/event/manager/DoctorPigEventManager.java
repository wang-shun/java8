package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.event.dao.DoctorEventRelationDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.GroupEventType;
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
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Created by xjn.
 * Date:2017-01-19
 * 猪事件管理
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
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorEventRelationDao doctorEventRelationDao;

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

    /**
     * 构建编辑事件
     * @param basic 基础数据
     * @param inputDto 输入事件
     * @return 编辑事件
     */
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEventHandler handler = getHandler(inputDto.getEventType());
        return handler.buildPigEvent(basic, inputDto);
    }

    /**
     * 构建事件执行后track
     * @param executeEvent 需要执行事件
     * @param fromTrack 执行前track
     * @return 事件执行后track
     */
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigEventHandler handler = getHandler(executeEvent.getType());
        return handler.buildPigTrack(executeEvent, fromTrack);
    }

    /**
     *  创建猪跟踪和镜像表
     *  @param toTrack 事件发生导致track
     *  @param executeEvent 发生事件
     *  @param lastEventId 上一次事件id
     *
     */
    public void createPigSnapshot(DoctorPigTrack toTrack, DoctorPigEvent executeEvent, Long lastEventId){
        getHandler(executeEvent.getType()).createPigSnapshot(toTrack, executeEvent, lastEventId);
    }

    @Transactional
    public void transactionalHandle(DoctorPigEventHandler handler, List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        handler.handle(doctorEventInfoList, executeEvent, fromTrack);
    }

    /**
     * 猪事件编辑错误时回滚
     * @param doctorEventInfoList 新事件列表
     * @param pigOldEventIdList 原事件id列
     * @param fromTrack 事件编辑前猪track
     */
    @Transactional
    public void modifyPidEventRollback(List<DoctorEventInfo> doctorEventInfoList, List<Long> pigOldEventIdList, DoctorPigTrack fromTrack, DoctorPig oldPig) {
        log.info("rollback.modify.failed, starting");
        log.info("doctorEventInfoList:{}, pigOldEventIdList:{}, fromTrack:{}, oldPig:{}", doctorEventInfoList, pigOldEventIdList, fromTrack, oldPig);
        //1.将新生成事件置为无效
        List<Long> pigNewEventIdList = doctorEventInfoList.stream()
                .filter(doctorEventInfo -> Objects.equals(doctorEventInfo.getBusinessType(), DoctorEventInfo.Business_Type.PIG.getValue()))
                .map(DoctorEventInfo::getEventId).collect(Collectors.toList());
        if (!Arguments.isNullOrEmpty(pigNewEventIdList)) {
            doctorPigEventDao.updateEventsStatus(pigNewEventIdList, EventStatus.INVALID.getValue());
        }
        //2.将原事件置为有效
        if (!Arguments.isNullOrEmpty(pigOldEventIdList)) {
            doctorPigEventDao.updateEventsStatus(pigOldEventIdList, EventStatus.VALID.getValue());
        }
        //3.还原track
        doctorPigTrackDao.update(fromTrack);
        //4.还原猪
        doctorPigDao.update(oldPig);
        //5.还原之前的关联关系
        if (!Arguments.isNullOrEmpty(pigNewEventIdList)) {
            doctorEventRelationDao.updatePigEventStatus(pigNewEventIdList, DoctorEventRelation.Status.INVALID.getValue());
            List<Long> pigCreateOldEventIdList = doctorEventInfoList.stream()
                    .filter(doctorEventInfo -> Objects.equals(doctorEventInfo.getBusinessType(), DoctorEventInfo.Business_Type.PIG.getValue()))
                    .map(DoctorEventInfo::getOldEventId).collect(Collectors.toList());
            doctorEventRelationDao.updatePigEventStatusUnderHandling(pigCreateOldEventIdList, DoctorEventRelation.Status.VALID.getValue());
        }
        log.info("rollback.modify.failed, ending");
    }

    /**
     * 批量事件处理
     *
     * @param eventInputs
     * @param basic
     * @return
     */
    @Transactional
    public List<DoctorEventInfo> batchEventsHandle(List<BasePigEventInputDto> eventInputs, DoctorBasicInputInfoDto basic) {
        log.info("batch pig event handle starting, event type:{}", eventInputs.get(0).getEventType());
        //校验输入数据的重复性
        eventRepeatCheck(eventInputs);

        DoctorPigEventHandler handler = getHandler(eventInputs.get(0).getEventType());
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
     * 根据事件类型获取时间处理器
     * @param eventType 事件类型
     * @return 事件处理器
     */
    public DoctorPigEventHandler getHandler(Integer eventType) {
       return Checks.expectNotNull(pigEventHandlers.getEventHandlerMap().get(eventType), "get.pig.handler.failed", eventType);

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
        List<String> inputPigCodeList = inputList.stream().map(BasePigEventInputDto::getPigCode).sorted().collect(Collectors.toList());

        for (int i = 0; i< inputPigCodeList.size()-1; i++) {
            if (inputPigCodeList.get(i).equals(inputPigCodeList.get(i+1))) {
                throw new InvalidException("batch.event.pigCode.not.repeat", inputPigCodeList.get(i));
            }
        }
    }
}
