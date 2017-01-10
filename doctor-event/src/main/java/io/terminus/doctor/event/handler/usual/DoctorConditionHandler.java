package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorConditionHandler extends DoctorAbstractEventHandler{

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        if (Objects.equals(inputDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
            DoctorConditionDto conditionDto = (DoctorConditionDto) inputDto;
            if (conditionDto.getConditionWeight() != null) {
                doctorPigTrack.setWeight(conditionDto.getConditionWeight());
            }
        } else {
            DoctorBoarConditionDto boarConditionDto = (DoctorBoarConditionDto) inputDto;
            checkState(boarConditionDto.getWeight() != null, "boar.condition.weight.not.null");
            doctorPigTrack.setWeight(boarConditionDto.getWeight());
        }

        return doctorPigTrack;
    }
}
