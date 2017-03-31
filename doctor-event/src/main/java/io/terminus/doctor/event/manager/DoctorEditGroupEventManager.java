package io.terminus.doctor.event.manager;

import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.enums.*;
import io.terminus.doctor.event.handler.group.DoctorGroupEventHandlers;
import io.terminus.doctor.event.handler.usual.DoctorEntryHandler;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:38 17/3/9
 */

@Slf4j
@Component
public class DoctorEditGroupEventManager {

    private DoctorGroupEventHandlers doctorGroupEventHandlers;
    private DoctorGroupEventDao doctorGroupEventDao;
    private DoctorGroupTrackDao doctorGroupTrackDao;
    private DoctorPigEventDao doctorPigEventDao;
    private DoctorEntryHandler doctorEntryHandler;
    private DoctorEventRelationDao doctorEventRelationDao;
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private DoctorGroupDao doctorGroupDao;
    private static JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    @Autowired
    public DoctorEditGroupEventManager(DoctorGroupEventHandlers doctorGroupEventHandlers,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao,
                                       DoctorPigEventDao doctorPigEventDao,
                                       DoctorEntryHandler doctorEntryHandler,
                                       DoctorEventRelationDao doctorEventRelationDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorGroupDao doctorGroupDao){
        this.doctorGroupEventHandlers = doctorGroupEventHandlers;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorEntryHandler = doctorEntryHandler;
        this.doctorEventRelationDao= doctorEventRelationDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupDao = doctorGroupDao;
    }

    @Transactional
    public void reElicitGroupEventByGroupId(Long groupId) {
        List<DoctorGroupEvent> groupEvents = doctorGroupEventDao.findLinkedGroupEventsByGroupId(groupId);
        if(Arguments.isNullOrEmpty(groupEvents)){
            log.error("group events info broken, groupId: {}", groupId);
            throw new InvalidException("group.events.info.broken", groupId);
        }
        DoctorGroupTrack track = doctorGroupTrackDao.findByGroupId(groupId);
        if(Arguments.isNull(track)){
            log.error("group track info broken, groupId: {}", groupId);
            throw new InvalidException("group.track.info.broken", groupId);
        }
        DoctorGroupTrack newTrack = new DoctorGroupTrack();
        newTrack.setId(track.getId());

        //按时间,id排序
        groupEvents = groupEvents.stream().sorted(
                (doctorGroupEvent1, doctorGroupEvent2)-> {
                    if(Dates.startOfDay(doctorGroupEvent1.getEventAt()).compareTo(Dates.startOfDay(doctorGroupEvent2.getEventAt())) == 0){
                        return doctorGroupEvent1.getId().compareTo(doctorGroupEvent2.getId());
                    }

                    return doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt());
                }
        ).collect(Collectors.toList());
        doctorGroupSnapshotDao.deleteByGroupId(groupId);

        Long fromEventId = 0L;
        for(DoctorGroupEvent doctorGroupEvent: groupEvents) {
            newTrack = doctorGroupEventHandlers.getEventHandlerMap().get(doctorGroupEvent.getType()).elicitGroupTrack(doctorGroupEvent, newTrack);
            if(!checkTrack(newTrack)){
                throw new InvalidException("group.track.info.broken", groupId);
            }
            createSnapshots(fromEventId, doctorGroupEvent, newTrack);
            fromEventId = doctorGroupEvent.getId();
        }
        doctorGroupTrackDao.update(newTrack);

        DoctorGroupEvent lastEvent = groupEvents.get(groupEvents.size() - 1);
        //track.quantity == 0 最后一个事件不是关闭猪群事件
        if(Objects.equals(newTrack.getQuantity(), 0) && !Objects.equals(lastEvent.getType(), GroupEventType.CLOSE.getValue())){
            closeGroupEvent(lastEvent);
        }
        //track.quantity != 0 最后一个事件是关闭猪群事件应该删除关闭猪群的事件
        if(!Objects.equals(newTrack.getQuantity(), 0) && Objects.equals(lastEvent.getType(), GroupEventType.CLOSE.getValue())){
            deleteCloseGroupEvent(lastEvent);
        }
    }

    private boolean checkTrack(DoctorGroupTrack newTrack) {
        if(newTrack.getQuantity() < 0 ){
            return false;
        }
        return true;
    }

    private void deleteCloseGroupEvent(DoctorGroupEvent lastEvent) {
        lastEvent.setStatus(EventStatus.INVALID.getValue());
        doctorGroupEventDao.update(lastEvent);
    }

    private void createSnapshots(Long fromEventId, DoctorGroupEvent doctorGroupEvent, DoctorGroupTrack newTrack) {
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(doctorGroupEvent.getId());
        if(!Arguments.isNull(snapshot)){
            doctorGroupSnapshotDao.delete(snapshot.getId());
        }
        DoctorGroupSnapshot newSnapshot = new DoctorGroupSnapshot();
        newSnapshot.setGroupId(doctorGroupEvent.getGroupId());
        newSnapshot.setFromEventId(fromEventId);
        newSnapshot.setToEventId(doctorGroupEvent.getId());
        DoctorGroupSnapShotInfo toInfo = DoctorGroupSnapShotInfo.builder().group(doctorGroupDao.findById(doctorGroupEvent.getGroupId()))
                .groupTrack(newTrack).build();
        newSnapshot.setToInfo(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(toInfo));
        doctorGroupSnapshotDao.create(newSnapshot);
    }

    public void closeGroupEvent(DoctorGroupEvent doctorGroupEvent) {
        DoctorGroup group = doctorGroupDao.findById(doctorGroupEvent.getGroupId());
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.update(group);
        doctorGroupEvent.setType(GroupEventType.CLOSE.getValue());
        doctorGroupEvent.setName(GroupEventType.CLOSE.getDesc());
        doctorGroupEvent.setDesc("【系统自动】");
        doctorGroupEvent.setIsAuto(IsOrNot.YES.getValue());
        doctorGroupEvent.setRelGroupEventId(doctorGroupEvent.getId());
        doctorGroupEvent.setExtra(null);
        doctorGroupEventDao.create(doctorGroupEvent);
    }

    @Transactional
    public void elicitDoctorGroupTrackRebuildOne(DoctorGroupEvent newEvent) {
        DoctorGroupEvent oldEvent = doctorGroupEventDao.findById(newEvent.getId());
        DoctorGroupEvent updateEvent = new DoctorGroupEvent();
        updateEvent.setId(oldEvent.getId());
        updateEvent.setStatus(EventStatus.INVALID.getValue());
        doctorGroupEventDao.update(updateEvent);
        doctorGroupEventDao.create(newEvent);
        updateRelation(oldEvent, newEvent); //更新事件关联关系
        reElicitGroupEventByGroupId(newEvent.getGroupId());
    }

    /**
     * 更新事件关联关系
     * @param oldEvent
     * @param newEvent
     */
    private void updateRelation(DoctorGroupEvent oldEvent, DoctorGroupEvent newEvent) {
        List<DoctorEventRelation> oldOriginRelations = doctorEventRelationDao.findByOrigin(oldEvent.getId());
        if(!Arguments.isNullOrEmpty(oldOriginRelations)){
            oldOriginRelations.forEach(doctorEventRelation -> {
                doctorEventRelation.setOriginEventId(newEvent.getId());
            });
            doctorEventRelationDao.batchUpdateStatus(oldOriginRelations.stream().map(DoctorEventRelation::getId).collect(Collectors.toList()), DoctorEventRelation.Status.INVALID.getValue());
            doctorEventRelationDao.creates(oldOriginRelations);
        }

        DoctorEventRelation oldTriggerRelation = doctorEventRelationDao.findByTrigger(oldEvent.getId());
        if(!Arguments.isNull(oldTriggerRelation)){
            doctorEventRelationDao.batchUpdateStatus(Lists.newArrayList(oldTriggerRelation.getId()), DoctorEventRelation.Status.INVALID.getValue());
            oldTriggerRelation.setTriggerEventId(newEvent.getId());
            doctorEventRelationDao.create(oldTriggerRelation);
        }
    }
}
