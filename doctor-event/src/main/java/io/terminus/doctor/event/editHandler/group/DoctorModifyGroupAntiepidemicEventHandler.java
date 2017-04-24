package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/22.
 * 防疫
 */
@Component
public class DoctorModifyGroupAntiepidemicEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorAntiepidemicGroupInput newInput = (DoctorAntiepidemicGroupInput) input;
        newEvent.setVaccinationId(newInput.getVaccinId());
        newEvent.setVaccinationName(newInput.getVaccinName());
        newEvent.setVaccinResult(newEvent.getVaccinResult());
        newEvent.setBasicId(newInput.getVaccinItemId());
        newEvent.setBasicName(newInput.getVaccinItemName());
        newEvent.setOperatorId(newInput.getVaccinStaffId());
        newEvent.setOperatorName(newInput.getVaccinStaffName());
        return newEvent;
    }
}
