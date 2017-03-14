package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorEventRelationDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.enums.EventElicitStatus;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.manager.DoctorEditGroupEventManager;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:45 17/3/8
 */

@Slf4j
@Service
@RpcProvider
public class DoctorEditGroupEventServiceImpl implements DoctorEditGroupEventService{

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private DoctorGroupReadService doctorGroupReadService;

    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    private DoctorEditGroupEventManager doctorEditGroupEventManager;

    private DoctorGroupTrackDao doctorGroupTrackDao;

    private DoctorGroupWriteService doctorGroupWriteService;

    private DoctorGroupEventDao doctorGroupEventDao;

    private DoctorEventRelationDao doctorEventRelationDao;


    @Autowired
    public DoctorEditGroupEventServiceImpl(DoctorGroupReadService doctorGroupReadService,
                                           DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorEditGroupEventManager doctorEditGroupEventManager,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorGroupWriteService doctorGroupWriteService,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorEventRelationDao doctorEventRelationDao){
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorEditGroupEventManager = doctorEditGroupEventManager;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorEventRelationDao = doctorEventRelationDao;
    }

    @Override
    public Response<String> elicitDoctorGroupTrack(DoctorGroupEvent doctorGroupEvent){
        List<DoctorGroupTrack> rollbackDoctorGroupTrackList = Lists.newArrayList();
        List<Long> rollbackDoctorGroupEventList = Lists.newArrayList();
        List<Long> taskDoctorGroupEventList = Lists.newArrayList();
        try{
            return elicitDoctorGroupEvents(doctorGroupEvent, rollbackDoctorGroupTrackList, rollbackDoctorGroupEventList, taskDoctorGroupEventList);
        }catch(Exception e){
            rollBackFailed(rollbackDoctorGroupTrackList, rollbackDoctorGroupEventList, taskDoctorGroupEventList);
            return Response.fail("edit group event failed");
        }

    }


    private Response<String> elicitDoctorGroupEvents(DoctorGroupEvent doctorGroupEvent, List<DoctorGroupTrack> rollbackDoctorGroupTrackList, List<Long> rollbackDoctorGroupEventList, List<Long> taskDoctorGroupEventList) {
        log.info("elicitDoctorGroupTrack start, doctorGroupEvent: {}", doctorGroupEvent);
        List<DoctorGroupEvent> triggerDoctorGroupEventList = Lists.newArrayList();
        List<DoctorGroupEvent> localDoctorGroupEventList = Lists.newArrayList();
        DoctorGroupTrack doctorGroupTrack = new DoctorGroupTrack();
        try {
            DoctorGroupEvent oldEvent = doctorGroupEventDao.findById(doctorGroupEvent.getId());
            rollbackDoctorGroupTrackList.add(doctorGroupTrackDao.findByGroupId(doctorGroupEvent.getGroupId()));

            //获取要重新推演的events list,不包括编辑的事件
            localDoctorGroupEventList = getTaskGroupEventList(localDoctorGroupEventList, doctorGroupEvent, doctorGroupTrack);

            //不是新增的事件,放入需要重新生成的列表中
            if(Objects.nonNull(doctorGroupEvent.getId())){
                taskDoctorGroupEventList.add(doctorGroupEvent.getId());
            }

            taskDoctorGroupEventList.addAll(localDoctorGroupEventList.stream().map(DoctorGroupEvent::getId).collect(Collectors.toList()));

            //处理第一个事件
            doctorGroupTrack = doctorEditGroupEventManager.elicitDoctorGroupTrack(triggerDoctorGroupEventList, rollbackDoctorGroupEventList, doctorGroupTrack, doctorGroupEvent);

            //将需要推演的groupEvent.status = 0
            doctorEditGroupEventManager.updateDoctorGroupEventStatus(taskDoctorGroupEventList, EventStatus.HANDLING.getValue());

            int index = 0;
            for(DoctorGroupEvent handlerDoctorGroupEvent: localDoctorGroupEventList){
                doctorGroupTrack = doctorEditGroupEventManager.elicitDoctorGroupTrack(triggerDoctorGroupEventList, rollbackDoctorGroupEventList, doctorGroupTrack, handlerDoctorGroupEvent);
                index ++;
                //如果doctorGroupTrack.quantity = 0 ,关闭猪群
                if(index == taskDoctorGroupEventList.size() && doctorGroupTrack.getQuantity() == 0){
                    closeGroupEvent(handlerDoctorGroupEvent);
                }
            }

            triggerEvents(oldEvent, doctorGroupEvent, rollbackDoctorGroupTrackList, rollbackDoctorGroupEventList, taskDoctorGroupEventList);

            doctorEditGroupEventManager.updateDoctorGroupEventStatus(taskDoctorGroupEventList, EventStatus.INVALID.getValue());

        }catch(Exception e){
            log.info("edit event failed, cause: {}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("edit.group.event.failed");
        }
        log.info("elicitDoctorGroupTrack end, doctorGroupTrack: {}", doctorGroupTrack);
        return Response.ok("编辑成功!!!");
    }

    private void triggerEvents(DoctorGroupEvent oldEvent, DoctorGroupEvent newEvent, List<DoctorGroupTrack> rollbackDoctorGroupTrackList, List<Long> rollbackDoctorGroupEventList, List<Long> taskDoctorGroupEventList) {
        //如果第一个事件是转种猪,触发母猪事件
        if(Objects.equals(GroupEventType.TURN_SEED.getValue(), oldEvent.getType())){
            doctorEditGroupEventManager.triggerPigEvents(newEvent);
        }
        if(Objects.equals(GroupEventType.TRANS_GROUP.getValue(), oldEvent.getType())){
            DoctorTransGroupEvent oldTransEvent = JSON_MAPPER.fromJson(oldEvent.getExtra(), DoctorTransGroupEvent.class);
            DoctorTransGroupEvent newTransEvent = JSON_MAPPER.fromJson(newEvent.getExtra(), DoctorTransGroupEvent.class);
            DoctorEventRelation rel = doctorEventRelationDao.findByOriginAndType(oldEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue());
            DoctorGroupEvent oldMoveInEvent = doctorGroupEventDao.findById(rel.getTriggerEventId());
            //1.转向猪舍没有变, 只改变数量、重量、日期
            //2.转向猪舍变了,删除原来的转入猪群(暂不支持)// TODO: 17/3/13
            //3.新增一个转入猪群
            if(!Objects.equals(oldTransEvent.getToBarnId(), newTransEvent.getToBarnId())){
                log.info("eable edit transgroup change barn");
                throw new JsonResponseException("enable.edit.trans.group.change.barn");
            }
            oldMoveInEvent = buildMoveInByTransGroup(oldMoveInEvent, newEvent);
            this.elicitDoctorGroupEvents(oldMoveInEvent, rollbackDoctorGroupTrackList, rollbackDoctorGroupEventList, taskDoctorGroupEventList);
        }

    }

    private DoctorGroupEvent buildMoveInByTransGroup(DoctorGroupEvent moveInGroupEvent, DoctorGroupEvent transGroupEvent) {
        DoctorMoveInGroupEvent oldMoveInEvent = JSON_MAPPER.fromJson(moveInGroupEvent.getExtra(), DoctorMoveInGroupEvent.class);
        moveInGroupEvent.setEventAt(transGroupEvent.getEventAt());
        moveInGroupEvent.setQuantity(transGroupEvent.getQuantity());
        moveInGroupEvent.setWeight(transGroupEvent.getWeight());
        moveInGroupEvent.setAvgWeight(transGroupEvent.getAvgWeight());
        moveInGroupEvent.setRemark(transGroupEvent.getRemark());
        oldMoveInEvent.setSowQty(transGroupEvent.getQuantity());
        oldMoveInEvent.setBoarQty(0);
        moveInGroupEvent.setExtraMap(oldMoveInEvent);
        return moveInGroupEvent;
    }


    private void closeGroupEvent(DoctorGroupEvent doctorGroupEvent) {
        doctorGroupEvent.setType(GroupEventType.CLOSE.getValue());
        doctorGroupEvent.setName(GroupEventType.CLOSE.getDesc());
        doctorGroupEvent.setDesc("【系统自动】");
        doctorGroupEvent.setExtra(null);
        doctorGroupWriteService.createGroupEvent(doctorGroupEvent);
    }

    private List<DoctorGroupEvent> getTaskGroupEventList(List<DoctorGroupEvent> taskDoctorGroupEventList, DoctorGroupEvent doctorGroupEvent, DoctorGroupTrack doctorGroupTrack) {

        List<DoctorGroupEvent> linkedDoctorGroupEventList = Lists.newArrayList();
        Response<List<DoctorGroupEvent>> doctorGroupEventResp = doctorGroupReadService.findLinkedGroupEventsByGroupId(doctorGroupEvent.getGroupId());
        if (!doctorGroupEventResp.isSuccess() || Arguments.isNullOrEmpty(doctorGroupEventResp.getResult())) {
            log.info("find linked group events failed, groupId: {}", doctorGroupEvent.getGroupId());
            throw new JsonResponseException("find.group.list.failed");
        }

        List<DoctorGroupEvent> doctorGroupEventList = doctorGroupEventResp.getResult();

        linkedDoctorGroupEventList = doctorGroupEventList.stream().filter(
                doctorGroupEvent1 -> {
                    if(Objects.nonNull(doctorGroupEvent.getId()) && Dates.startOfDay(doctorGroupEvent1.getEventAt()).compareTo(Dates.startOfDay(doctorGroupEvent.getEventAt())) == 0){
                        return doctorGroupEvent1.getId().compareTo(doctorGroupEvent.getId()) == 1;
                    }
                    return Dates.startOfDay(doctorGroupEvent1.getEventAt()).compareTo(Dates.startOfDay(doctorGroupEvent.getEventAt())) == 1;
                }
        ).sorted(
                (doctorGroupEvent1, doctorGroupEvent2)-> {
                    if(doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt()) == 0){
                        return doctorGroupEvent1.getType().compareTo(doctorGroupEvent2.getType());
                    }

                    return doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt());
                }
        ).collect(Collectors.toList());

        DoctorGroupEvent preDoctorGroupEvent = doctorGroupEventList.stream().filter(
                doctorGroupEvent1 -> {
                    if(Objects.nonNull(doctorGroupEvent.getId()) && Dates.startOfDay(doctorGroupEvent1.getEventAt()).compareTo(Dates.startOfDay(doctorGroupEvent.getEventAt())) == 0){
                        return doctorGroupEvent1.getId().compareTo(doctorGroupEvent.getId()) == -1;
                    }
                    return Dates.startOfDay(doctorGroupEvent1.getEventAt()).compareTo(Dates.startOfDay(doctorGroupEvent.getEventAt())) <= 0;
                })
                .sorted((doctorGroupEvent1, doctorGroupEvent2)-> doctorGroupEvent2.getId().compareTo(doctorGroupEvent1.getId()))
                .findFirst()
                .get();

        //要编辑的事件不是第一个事件
        if (!Arguments.isNull(preDoctorGroupEvent)) {
            DoctorGroupSnapshot preDoctorGroupSnapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(preDoctorGroupEvent.getId());
            if (Arguments.isNull(preDoctorGroupSnapshot)) {
                log.info("find DoctorGroupSnapshot failed, no DoctorGroupSnapshot, toEventId={}", preDoctorGroupEvent.getId());
                throw new JsonResponseException("group.snapshot.info.broken");
            }
            DoctorGroupSnapShotInfo doctorGroupSnapShotInfo = JSON_MAPPER.fromJson(preDoctorGroupSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);
            if (Arguments.isNull(doctorGroupSnapShotInfo) || Arguments.isNull(doctorGroupSnapShotInfo.getGroupTrack())) {
                log.info("DoctorGroupSnapShotInfo broken, toEventId={}", preDoctorGroupEvent.getId());
                throw new JsonResponseException("group.snapshot.info.broken");
            }
            BeanMapper.copy(doctorGroupSnapShotInfo.getGroupTrack(), doctorGroupTrack);
        }
        taskDoctorGroupEventList.addAll(linkedDoctorGroupEventList);
        return taskDoctorGroupEventList;
    }


    private void rollBackFailed(List<DoctorGroupTrack> doctorGroupTrackList, List<Long> doctorGroupEvents, List<Long> taskDoctorGroupEvents){
        log.info("rollback group track, doctorGroupTrackList = {}", doctorGroupTrackList);
        log.info("rollback group event, groupEventList = {}", doctorGroupEvents);
        log.info("rollback new group event, taskDoctorGroupEvents = {}", taskDoctorGroupEvents);
        doctorEditGroupEventManager.rollbackElicitEvents(doctorGroupTrackList, doctorGroupEvents, taskDoctorGroupEvents);
    }

}
