package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 状态新事件的构建
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifyPigConditionEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        //1.公猪体况
        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())) {
            DoctorBoarConditionDto boarConditionDto = (DoctorBoarConditionDto) inputDto;
            newEvent.setWeight(boarConditionDto.getWeight());
            return newEvent;
        }
        //2.母猪体况
        DoctorConditionDto conditionDto = (DoctorConditionDto) inputDto;
        if (conditionDto.getConditionWeight() != null) {
            newEvent.setWeight(conditionDto.getConditionWeight());
        }
        return newEvent;
    }

}
