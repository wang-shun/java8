package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 防疫新事件的构建
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifyPigVaccinEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorVaccinationDto doctorVaccinationDto = (DoctorVaccinationDto) inputDto;
        doctorPigEvent.setVaccinationId(doctorVaccinationDto.getVaccinationId());
        doctorPigEvent.setVaccinationName(doctorVaccinationDto.getVaccinationName());
        doctorPigEvent.setBasicId(doctorVaccinationDto.getVaccinationItemId());
        doctorPigEvent.setBasicName(doctorVaccinationDto.getVaccinationItemName());
        return doctorPigEvent;
    }
}
