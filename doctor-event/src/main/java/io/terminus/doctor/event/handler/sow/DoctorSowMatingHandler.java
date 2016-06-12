package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorSowMatingHandler extends DoctorAbstractEventFlowHandler{

    @Autowired
    public DoctorSowMatingHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao, DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao, DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public void specialFlowHandler(Execution execution, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        Long boarId = Long.valueOf(extra.get("matingBoarPigId").toString());
        DoctorPigTrack doctorPigTrack = this.doctorPigTrackDao.findByPigId(boarId);
        checkState(!isNull(doctorPigTrack), "createMating.boarPigId.fail");
        Integer currentBoarParity = MoreObjects.firstNonNull(doctorPigTrack.getCurrentParity(), 0) + 1;
        doctorPigTrack.setCurrentParity(currentBoarParity);
        doctorPigTrackDao.update(doctorPigTrack);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {
        // 构建母猪配种信息
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.setStatus(PigStatus.Mate.getKey());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
