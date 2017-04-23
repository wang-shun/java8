package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 疾病新事件的创建
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifyPigDiseaseEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorDiseaseDto diseaseDto = (DoctorDiseaseDto) inputDto;
        DoctorPigEvent doctorPigEvent = super.buildNewEvent(oldPigEvent, inputDto);
        doctorPigEvent.setBasicId(diseaseDto.getDiseaseId());
        doctorPigEvent.setBasicName(diseaseDto.getDiseaseName());
        return doctorPigEvent;
    }
}
