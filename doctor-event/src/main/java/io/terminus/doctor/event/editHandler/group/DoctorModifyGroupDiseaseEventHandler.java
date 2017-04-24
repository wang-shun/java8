package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/22.
 * 疾病
 */
@Component
public class DoctorModifyGroupDiseaseEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorDiseaseGroupInput newInput = (DoctorDiseaseGroupInput) input;
        newEvent.setBasicId(newInput.getDiseaseId());
        newEvent.setBasicName(newInput.getDiseaseName());
        newEvent.setOperatorId(newInput.getDoctorId());
        newEvent.setOperatorName(newInput.getDoctorName());
        return newEvent;
    }
}
