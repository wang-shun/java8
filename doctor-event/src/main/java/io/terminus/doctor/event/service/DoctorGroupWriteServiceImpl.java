package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 * Desc: 猪群卡片表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupWriteServiceImpl implements DoctorGroupWriteService {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupManager doctorGroupManager;

    @Autowired
    public DoctorGroupWriteServiceImpl(DoctorGroupDao doctorGroupDao,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao,
                                       DoctorGroupManager doctorGroupManager) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupManager = doctorGroupManager;
    }

    @Override
    public Response<Long> createNewGroup(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack) {
        try {
            return Response.ok(doctorGroupManager.createNewGroup(group, groupEvent, groupTrack));
        } catch (Exception e) {
            log.error("create group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventAntiepidemic(DoctorGroup group, @Valid DoctorAntiepidemicGroupInput antiepidemic) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventChange(DoctorGroup group, @Valid DoctorChangeGroupInput change) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventClose(DoctorGroup group, @Valid DoctorCloseGroupInput close) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventDisease(DoctorGroup group, @Valid DoctorDiseaseGroupInput disease) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventLiveStock(DoctorGroup group, @Valid DoctorLiveStockGroupInput liveStock) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventMoveIn(DoctorGroup group, @Valid DoctorMoveInGroupInput moveIn) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventTransFarm(DoctorGroup group, @Valid DoctorTransFarmGroupInput transFarm) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventTransGroup(DoctorGroup group, @Valid DoctorTransGroupInput transGroup) {
        return null;
    }

    @Override
    public Response<Boolean> groupEventTurnSeed(DoctorGroup group, @Valid DoctorTurnSeedGroupInput turnSeed) {
        return null;
    }

}
