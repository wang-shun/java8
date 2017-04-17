package io.terminus.doctor.event.editHandler.pig;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.event.dto.DoctorBasicInputInfoDto.generateEventDescFromExtra;

/**
 * 采精的新事件的构建
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifySemenEventHandler extends DoctorAbstractModifyPigEventHandler{
    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorSemenDto doctorSemenDto = (DoctorSemenDto) inputDto;
        DoctorPigEvent doctorPigEvent = new DoctorPigEvent();
        BeanMapper.copy(oldPigEvent, doctorPigEvent);
        doctorPigEvent.setEventAt(doctorSemenDto.getSemenDate());
        doctorPigEvent.setRemark(doctorSemenDto.getSemenRemark());
        doctorPigEvent.setDesc(generateEventDescFromExtra(doctorSemenDto));
        doctorPigEvent.setExtra(TO_JSON_MAPPER.toJson(doctorSemenDto));
        return doctorPigEvent;
    }
}
