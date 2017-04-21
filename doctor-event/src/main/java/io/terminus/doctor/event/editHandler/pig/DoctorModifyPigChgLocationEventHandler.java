package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransGroupEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 17/4/19.
 * 转舍
 */
@Component
public class DoctorModifyPigChgLocationEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Autowired
    private DoctorModifyGroupTransGroupEventHandler doctorModifyGroupTransGroupEventHandler;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorChgLocationDto newDto = (DoctorChgLocationDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldPigEvent.getEventAt())
                .newEventAt(inputDto.eventAt())
                .toBarnId(newDto.getChgLocationToBarnId())
                .build();
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        DoctorBarn doctorBarn = doctorBarnDao.findById(changeDto.getToBarnId());
        oldPigTrack.setCurrentBarnId(doctorBarn.getId());
        oldPigTrack.setCurrentBarnName(doctorBarn.getName());
        oldPigTrack.setCurrentBarnType(doctorBarn.getPigType());
        return oldPigTrack;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        if (Objects.equals(deletePigEvent.getType(), PigEvent.CHG_LOCATION.getKey())
                && Objects.equals(deletePigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            DoctorGroupEvent transGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.TRANS_GROUP.getValue());
            doctorModifyGroupTransGroupEventHandler.rollbackHandle(transGroupEvent, operatorId, operatorName);
        }
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setCurrentBarnId(deletePigEvent.getBarnId());
        oldPigTrack.setCurrentBarnName(deletePigEvent.getBarnName());
        oldPigTrack.setCurrentBarnType(deletePigEvent.getBarnType());
        return oldPigTrack;
    }
}
