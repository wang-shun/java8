package io.terminus.doctor.event.handler.group;

import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorTurnSeedGroupEventHandler extends DoctorAbstractGroupEventHandler {
    
    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorTurnSeedGroupEventHandler(DoctorGroupDao doctorGroupDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorGroupReadService doctorGroupReadService,
                                           CoreEventDispatcher coreEventDispatcher,
                                           DoctorGroupWriteService doctorGroupWriteService) {
        super(doctorGroupDao, doctorGroupEventDao, doctorGroupSnapshotDao, doctorGroupTrackDao,
                doctorGroupReadService, coreEventDispatcher, doctorGroupWriteService);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }
    
    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        // TODO: 商品猪转为种猪不能手动录入
    }
}
