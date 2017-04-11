package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorVaccinationHandler extends DoctorAbstractEventHandler{
    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorVaccinationDto vaccinationDto = (DoctorVaccinationDto)inputDto;
        doctorPigEvent.setBasicId(vaccinationDto.getVaccinationItemId());
        doctorPigEvent.setBasicName(vaccinationDto.getVaccinationItemName());
        doctorPigEvent.setVaccinationId(vaccinationDto.getVaccinationId());
        doctorPigEvent.setVaccinationName(vaccinationDto.getVaccinationName());
        return doctorPigEvent;
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        return fromTrack;
    }
}
