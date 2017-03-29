package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorEventRelationDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.manager.DoctorEditGroupEventManager;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


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

    private DoctorGroupDao doctorGroupDao;


    @Autowired
    public DoctorEditGroupEventServiceImpl(DoctorGroupReadService doctorGroupReadService,
                                           DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorEditGroupEventManager doctorEditGroupEventManager,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorGroupWriteService doctorGroupWriteService,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorEventRelationDao doctorEventRelationDao,
                                           DoctorGroupDao doctorGroupDao){
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorEditGroupEventManager = doctorEditGroupEventManager;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorEventRelationDao = doctorEventRelationDao;
        this.doctorGroupDao = doctorGroupDao;
    }

    @Override
    public Response<Boolean> reElicitGroupEvent(List<Long> groupIds) {
        try{
            groupIds.forEach(id -> {
                doctorEditGroupEventManager.reElicitGroupEventByGroupId(id);
            });
        } catch(InvalidException e) {
            throw e;
        }catch(Exception e){
            log.error("elicit group event failed, groupIds: {} , cause: {}", groupIds, Throwables.getStackTraceAsString(e));
            return Response.fail("elicit.group.event.failed");
        }
        return Response.ok();
    }

    @Override
    public void elicitDoctorGroupTrackRebuildOne(DoctorGroupEvent doctorGroupEvent) {
        try{
            beforeCheck(doctorGroupEvent);
            doctorEditGroupEventManager.elicitDoctorGroupTrackRebuildOne(doctorGroupEvent);
        }catch(InvalidException e){
            throw e;
        }catch(Exception e){
            log.error("elicit group event failed, doctorGroupEvent: {}", doctorGroupEvent);
            throw e;
        }
    }

    private void beforeCheck(DoctorGroupEvent newEvent) {
        DoctorGroupEvent oldEvent = doctorGroupEventDao.findEventById(newEvent.getId());
        DoctorGroupEvent initGroupEvent = doctorGroupEventDao.findInitGroupEvent(newEvent.getGroupId());
        if(Dates.startOfDay(newEvent.getEventAt()).compareTo(Dates.startOfDay(initGroupEvent.getEventAt())) == -1){
            log.info("eventAt less than group createdAt, groupId = {}", newEvent.getGroupId());
            throw new InvalidException("eventat.less.than.group.createdat", newEvent.getGroupCode(), DateUtil.getDateStr(initGroupEvent.getEventAt()));
        }
        DoctorGroupTrack track = doctorGroupTrackDao.findByGroupId(newEvent.getGroupId());
        DoctorGroup group = doctorGroupDao.findById(newEvent.getGroupId());
        if(Objects.equals(DoctorGroup.Status.CLOSED.getValue(), group.getStatus())){
            log.info("group has been closed, groupId = {}", newEvent.getGroupId());
            throw new InvalidException("group.has.been.closed", newEvent.getGroupCode());
        }
        if(Objects.equals(GroupEventType.WEAN.getValue(), newEvent.getType()) &&
                track.getQuantity() - track.getUnweanQty() + newEvent.getQuantity() - oldEvent.getQuantity()  < 0){
            log.info("group quantity not enough, groupId = {}", newEvent.getGroupId());
            throw new InvalidException("group.quantity.not.enough", newEvent.getGroupCode(), track.getQuantity(),  Math.abs(oldEvent.getQuantity() - newEvent.getQuantity()));
        }
        if(Objects.equals(GroupEventType.MOVE_IN.getValue(), newEvent.getType())  &&
                track.getQuantity() + newEvent.getQuantity() - oldEvent.getQuantity()  < 0){
            log.info("group quantity not enough, groupId = {}", newEvent.getGroupId());
            throw new InvalidException("group.quantity.not.enough", newEvent.getGroupCode(), track.getQuantity(),  Math.abs(oldEvent.getQuantity() - newEvent.getQuantity()));
        }
        if(Objects.equals(GroupEventType.CHANGE.getValue(), newEvent.getType()) &&
                track.getQuantity() + oldEvent.getQuantity() - newEvent.getQuantity() < 0){
            log.info("group quantity not enough, groupId = {}", newEvent.getGroupId());
            throw new InvalidException("group.quantity.not.enough", newEvent.getGroupCode(), track.getQuantity(), Math.abs(newEvent.getQuantity() - oldEvent.getQuantity()));
        }


    }

}
