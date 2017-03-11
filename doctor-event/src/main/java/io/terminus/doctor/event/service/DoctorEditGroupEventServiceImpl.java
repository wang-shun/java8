package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.editHandler.group.DoctorEditGroupEventHandlers;
import io.terminus.doctor.event.enums.EventElicitStatus;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.manager.DoctorEditGroupEventManager;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
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

    private DoctorEditGroupEventHandlers doctorEditGroupEventHandlers;

    private DoctorGroupReadService doctorGroupReadService;

    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    private DoctorEditGroupEventManager doctorEditGroupEventManager;

    private DoctorGroupTrackDao doctorGroupTrackDao;

    private DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorEditGroupEventServiceImpl(DoctorEditGroupEventHandlers doctorEditGroupEventHandlers,
                                           DoctorGroupReadService doctorGroupReadService,
                                           DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorEditGroupEventManager doctorEditGroupEventManager,
                                           DoctorGroupTrackDao doctorGroupTrackDao){
        this.doctorEditGroupEventHandlers = doctorEditGroupEventHandlers;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorEditGroupEventManager = doctorEditGroupEventManager;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    public Response<Boolean> elicitDoctorGroupTrack(DoctorGroupEvent doctorGroupEvent, EventElicitStatus flag) {
        log.info("elicitDoctorGroupTrack start, doctorGroupEvent: {}", doctorGroupEvent);
        List<DoctorGroupTrack> rollbackDoctorGroupTrackList = Lists.newArrayList();
        List<DoctorGroupEvent> rollbackDoctorGroupEventList = Lists.newArrayList();
        List<DoctorGroupEvent> taskDoctorGroupEventList = Lists.newArrayList();
        DoctorGroupTrack doctorGroupTrack = new DoctorGroupTrack();
        try {

            rollbackDoctorGroupTrackList.add(doctorGroupTrackDao.findByGroupId(doctorGroupEvent.getGroupId()));
            //获取要重新推演的events list
            getTaskGroupEventList(taskDoctorGroupEventList, doctorGroupEvent, doctorGroupTrack, flag);

            //将需要推演的groupEvent.status = 0
            doctorEditGroupEventManager.updateOldDoctorGroupEvents(taskDoctorGroupEventList);

            int index = 0;
            for(DoctorGroupEvent handlerDoctorGroupEvent: taskDoctorGroupEventList){
                doctorGroupTrack = doctorEditGroupEventManager.elicitDoctorGroupTrack(rollbackDoctorGroupEventList, doctorGroupTrack, handlerDoctorGroupEvent);
                index ++;
                //如果doctorGroupTrack.quantity = 0 ,关闭猪群
                if(index == taskDoctorGroupEventList.size() && doctorGroupTrack.getQuantity() == 0){
                    closeGroupEvent(handlerDoctorGroupEvent);
                }
            }

        }catch(Exception e){
            log.info("edit event failed, cause: {}", Throwables.getStackTraceAsString(e));
            rollBackFailed(rollbackDoctorGroupTrackList, rollbackDoctorGroupEventList, taskDoctorGroupEventList);
            throw new JsonResponseException("edit.group.event.failed");
        }

        return Response.ok();
    }

    private void closeGroupEvent(DoctorGroupEvent doctorGroupEvent) {
        doctorGroupEvent.setType(GroupEventType.CLOSE.getValue());
        doctorGroupEvent.setName(GroupEventType.CLOSE.getDesc());
        doctorGroupEvent.setDesc("【系统自动】");
        doctorGroupEvent.setExtra(null);
        doctorGroupWriteService.createGroupEvent(doctorGroupEvent);
    }

    private void getTaskGroupEventList(List<DoctorGroupEvent> taskDoctorGroupEventList, DoctorGroupEvent doctorGroupEvent, DoctorGroupTrack doctorGroupTrack, EventElicitStatus flag) {

        List<DoctorGroupEvent> linkedDoctorGroupEventList = Lists.newArrayList();
        Response<List<DoctorGroupEvent>> doctorGroupEventResp = doctorGroupReadService.findLinkedGroupEventsByGroupId(doctorGroupEvent.getGroupId());
        if (!doctorGroupEventResp.isSuccess() || Arguments.isNullOrEmpty(doctorGroupEventResp.getResult())) {
            log.info("find linked group events failed, groupId: {}", doctorGroupEvent.getGroupId());
            throw new JsonResponseException("find.group.list.failed");
        }

        List<DoctorGroupEvent> doctorGroupEventList = doctorGroupEventResp.getResult();

        linkedDoctorGroupEventList = doctorGroupEventList.stream().filter(
                doctorGroupEvent1 -> doctorGroupEvent1.getId() != doctorGroupEvent.getId() && doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent.getEventAt()) >= 0
        ).collect(Collectors.toList());
        if(Objects.equals(flag, EventElicitStatus.ADD) || Objects.equals(flag, EventElicitStatus.EDIT)){
            taskDoctorGroupEventList.add(doctorGroupEvent);
        }

        linkedDoctorGroupEventList = linkedDoctorGroupEventList.stream().sorted(
                (doctorGroupEvent1, doctorGroupEvent2)-> {
                    if(doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt()) == 0){
                        return doctorGroupEvent1.getType().compareTo(doctorGroupEvent2.getType());
                    }

                    return doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt());
                }).collect(Collectors.toList());
        DoctorGroupEvent preDoctorGroupEvent = doctorGroupEventList.stream().filter(doctorGroupEvent1 -> doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent.getEventAt()) == -1)
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
    }


    private void rollBackFailed(List<DoctorGroupTrack> doctorGroupTrackList, List<DoctorGroupEvent> doctorGroupEvents, List<DoctorGroupEvent> taskDoctorGroupEvents){
        log.info("rollback group track, doctorGroupTrackList = {}", doctorGroupTrackList);
        log.info("rollback group event, groupEventList = {}", doctorGroupEvents);
        log.info("rollback new group event, taskDoctorGroupEvents = {}", taskDoctorGroupEvents);
        doctorEditGroupEventManager.rollbackElicitEvents(doctorGroupTrackList, doctorGroupEvents, taskDoctorGroupEvents);
    }

}
