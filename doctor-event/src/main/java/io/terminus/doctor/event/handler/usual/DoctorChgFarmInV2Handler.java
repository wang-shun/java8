package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmInEventV2Handler;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/4/20.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorChgFarmInV2Handler extends DoctorAbstractEventHandler {
    @Autowired
    private DoctorModifyPigChgFarmInEventV2Handler modifyPigChgFarmInEventHandler;

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        doctorPigEvent.setRelEventId(inputDto.getRelPigEventId());
        return doctorPigEvent;
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        DoctorChgFarmDto doctorChgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        toTrack.setFarmId(doctorChgFarmDto.getToFarmId());
        DoctorBarn doctorBarn = doctorBarnDao.findById(doctorChgFarmDto.getToBarnId());
        toTrack.setCurrentBarnId(doctorBarn.getId());
        toTrack.setCurrentBarnName(doctorBarn.getName());
        toTrack.setCurrentBarnType(doctorBarn.getPigType());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        super.specialHandle(executeEvent, toTrack);
        DoctorPig doctorPig = doctorPigDao.findById(toTrack.getPigId());
        doctorPig.setFarmId(executeEvent.getFarmId());
        doctorPig.setFarmName(executeEvent.getFarmName());
        DoctorBarn doctorBarn = doctorBarnDao.findById(executeEvent.getBarnId());
        doctorPig.setInitBarnId(doctorBarn.getId());
        doctorPig.setInitBarnName(doctorBarn.getName());
        doctorPig.setInFarmDate(executeEvent.getEventAt());
        doctorPigDao.update(doctorPig);
    }

    @Override
    protected void updateDailyForNew(DoctorPigEvent newPigEvent) {
        DoctorChgFarmDto doctorChgFarmDto = JSON_MAPPER.fromJson(newPigEvent.getExtra(), DoctorChgFarmDto.class);
        modifyPigChgFarmInEventHandler.updateDailyOfNew(newPigEvent, doctorChgFarmDto);
    }
}
