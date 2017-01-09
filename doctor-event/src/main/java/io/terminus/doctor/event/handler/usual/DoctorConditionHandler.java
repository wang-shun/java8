package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

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
        DoctorConditionDto conditionDto = (DoctorConditionDto) inputDto;
        if (conditionDto.getConditionWeight() != null) {
            doctorPigTrack.setWeight(conditionDto.getConditionWeight());
        } else if (conditionDto.getWeight() != null) {
            doctorPigTrack.setWeight(conditionDto.getWeight());
        }
        //doctorPigTrack.addAllExtraMap(conditionDto.toMap());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));

        return doctorPigTrack;
    }
}
