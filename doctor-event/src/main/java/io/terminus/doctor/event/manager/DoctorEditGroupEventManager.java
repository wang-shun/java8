package io.terminus.doctor.event.manager;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.editHandler.group.DoctorEditGroupEventHandlers;
import io.terminus.doctor.event.enums.EventStatus;
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

    private DoctorEditGroupEventHandlers doctorEditGroupEventHandlers;
    private DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorEditGroupEventManager(DoctorEditGroupEventHandlers doctorEditGroupEventHandlers,
                                       DoctorGroupEventDao doctorGroupEventDao){
        this.doctorEditGroupEventHandlers = doctorEditGroupEventHandlers;
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Transactional
    public Boolean updateOldDoctorGroupEvents(List<DoctorGroupEvent> doctorGroupEventList){
        List<Long> ids = doctorGroupEventList.stream().map(DoctorGroupEvent::getId).collect(Collectors.toList());
        return doctorGroupEventDao.updateGroupEventStatus(ids, EventStatus.HANDLING.getValue());
    }

    @Transactional
    public DoctorGroupTrack elicitDoctorGroupTrack(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent){
        return doctorEditGroupEventHandlers.getEventHandlerMap().get(doctorGroupEvent.getType()).handle(doctorGroupTrack, doctorGroupEvent);
    }

}
