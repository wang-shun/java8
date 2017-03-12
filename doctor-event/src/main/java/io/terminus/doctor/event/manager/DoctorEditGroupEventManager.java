package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.handler.group.DoctorGroupEventHandlers;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Autowired
    public DoctorEditGroupEventManager(DoctorGroupEventHandlers doctorGroupEventHandlers,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao){
        this.doctorGroupEventHandlers = doctorGroupEventHandlers;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Transactional
    public Boolean updateOldDoctorGroupEvents(List<DoctorGroupEvent> doctorGroupEventList){
        List<Long> ids = doctorGroupEventList.stream().map(DoctorGroupEvent::getId).collect(Collectors.toList());
        return doctorGroupEventDao.updateGroupEventStatus(ids, EventStatus.HANDLING.getValue());
    }

    @Transactional
    public DoctorGroupTrack elicitDoctorGroupTrack(List<DoctorGroupEvent> doctorGroupEventList, DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent){
        return doctorGroupEventHandlers.getEventHandlerMap().get(doctorGroupEvent.getType()).editGroupEvent(doctorGroupEventList, doctorGroupTrack, doctorGroupEvent);
    }


    @Transactional
    public Boolean rollbackElicitEvents(List<DoctorGroupTrack> doctorGroupTrackList, List<DoctorGroupEvent> newDoctorGroupEvents, List<DoctorGroupEvent> oldDoctorGroupEvents) {
        Boolean status = true;
        List<Long> oldEventIds = oldDoctorGroupEvents.stream().map(DoctorGroupEvent::getId).collect(Collectors.toList());
        status = status && doctorGroupEventDao.updateGroupEventStatus(oldEventIds, EventStatus.VALID.getValue());
        List<Long> newEventIds = newDoctorGroupEvents.stream().map(DoctorGroupEvent::getId).collect(Collectors.toList());
        status = status && doctorGroupEventDao.updateGroupEventStatus(oldEventIds, EventStatus.INVALID.getValue());
        doctorGroupTrackList.stream().forEach(doctorGroupTrack -> doctorGroupTrackDao.update(doctorGroupTrack));
        return status;
    }
}
