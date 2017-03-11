package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:01 17/3/10
 */
@Slf4j
@Component
public class DoctorEditTurnSeedGroupEventHandler extends DoctorAbstractEditGroupEventHandler {


    @Override
    protected boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        if(doctorGroupTrack.getQuantity() < 1){
            return false;
        }
        return true;
    }

    @Override
    protected void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {
        DoctorTurnSeedGroupEvent doctorTurnSeedGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorTurnSeedGroupEvent.class);
        if(Arguments.isNull(doctorTurnSeedGroupEvent)) {
            log.error("parse doctorTurnSeedGroupEvent faild, doctorGroupEvent = {}", doctorGroupEvent);
            throw new JsonResponseException("group.event.info.broken");
        }

        DoctorPig.PigSex sex = getSex(doctorTurnSeedGroupEvent.getToBarnType());

        doctorGroupTrack.setQuantity(doctorGroupTrack.getQuantity() - 1);
        doctorGroupTrack.setBoarQty(getBoarQty(sex, doctorGroupTrack.getBoarQty()));
        doctorGroupTrack.setSowQty(doctorGroupTrack.getQuantity() - doctorGroupTrack.getBoarQty());

    }
}
