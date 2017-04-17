package io.terminus.doctor.event.editHandler.pig;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.event.dto.DoctorBasicInputInfoDto.generateEventDescFromExtra;

/**
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifyPigMatingEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {

        DoctorMatingDto doctorMatingDto = (DoctorMatingDto) inputDto;
        DoctorPigEvent doctorPigEvent = new DoctorPigEvent();
        BeanMapper.copy(oldPigEvent, doctorPigEvent);
        doctorPigEvent.setRemark(doctorMatingDto.getMattingMark());
        doctorPigEvent.setEventAt(doctorMatingDto.getMatingDate());
        doctorPigEvent.setMateType(doctorMatingDto.getMatingType());
        doctorPigEvent.setBoarCode(doctorMatingDto.getMatingBoarPigCode());
        doctorPigEvent.setOperatorName(doctorMatingDto.getOperatorName());
        doctorPigEvent.setOperatorId(doctorMatingDto.getOperatorId());
        doctorPigEvent.setDesc(TO_JSON_MAPPER.toJson(doctorMatingDto));
        doctorPigEvent.setExtra(generateEventDescFromExtra(doctorMatingDto));
        return doctorPigEvent;

    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorMatingDto newDoctorMatingDto = (DoctorMatingDto) inputDto;
        DoctorMatingDto oldDoctorMatingDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorMatingDto.class);
        return DoctorEventChangeDto.builder()
                .remark(newDoctorMatingDto.getMattingMark())
                .oldEventAt(oldDoctorMatingDto.eventAt())
                .newEventAt(newDoctorMatingDto.eventAt())
                .mateType(newDoctorMatingDto.getMatingType())
                .matingBoarCode(newDoctorMatingDto.getMatingBoarPigCode())
                .matingBoarId(newDoctorMatingDto.getMatingBoarPigId())
                .newOperatorId(newDoctorMatingDto.getOperatorId())
                .newOperatorName(newDoctorMatingDto.getOperatorName())
                .build();
    }
}
