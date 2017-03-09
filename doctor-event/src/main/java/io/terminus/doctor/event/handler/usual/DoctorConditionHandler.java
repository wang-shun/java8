package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorConditionHandler extends DoctorAbstractEventHandler{

    @Override
    protected DoctorPigTrack buildPigTrack(DoctorPigEvent inputEvent, DoctorPigTrack doctorPigTrack) {
        if (Objects.equals(doctorPigTrack.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
            DoctorConditionDto conditionDto = JSON_MAPPER.fromJson(inputEvent.getExtra(), DoctorConditionDto.class);
            if (conditionDto.getConditionWeight() != null) {
                doctorPigTrack.setWeight(conditionDto.getConditionWeight());
            }
        } else {
            DoctorBoarConditionDto boarConditionDto = JSON_MAPPER.fromJson(inputEvent.getExtra(), DoctorBoarConditionDto.class);
            doctorPigTrack.setWeight(boarConditionDto.getWeight());
        }

        return doctorPigTrack;
    }
}
