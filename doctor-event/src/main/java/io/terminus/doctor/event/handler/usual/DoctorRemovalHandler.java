package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorRemovalHandler extends DoctorAbstractEventHandler{

    @Autowired
    public DoctorRemovalHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao, DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao, DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        return Objects.equals(basic.getEventType(), PigEvent.REMOVAL.getKey());
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        if(Objects.equals(DoctorPig.PIG_TYPE.BOAR.getKey(), basic.getPigType())){
            doctorPigTrack.setStatus(PigStatus.BOAR_LEAVE.getKey());
        }else if(Objects.equals(DoctorPig.PIG_TYPE.SOW.getKey(), basic.getPigType())){
            doctorPigTrack.setStatus(PigStatus.Removal.getKey());
        }else {
            throw new IllegalStateException("basic.pigTypeValue.error");
        }
        return doctorPigTrack;
    }

    @Override
    public void afterHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        // 离场 事件 修改Pig 状态信息
        DoctorPig doctorPig = doctorPigDao.findById(basic.getPigId());
        checkState(!isNull(doctorPig), "input.doctorPigId.error");
        checkState(doctorPigDao.removalPig(doctorPig.getId()), "update.pigRemoval.fail");
    }
}
