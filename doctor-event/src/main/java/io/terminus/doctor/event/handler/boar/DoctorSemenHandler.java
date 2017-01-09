package io.terminus.doctor.event.handler.boar;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
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
public class DoctorSemenHandler extends DoctorAbstractEventHandler{

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorSemenDto semenDto = (DoctorSemenDto) inputDto;
        doctorPigTrack.setWeight(semenDto.getSemenWeight());
        //doctorPigTrack.addAllExtraMap(semenDto.toMap());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) content.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
