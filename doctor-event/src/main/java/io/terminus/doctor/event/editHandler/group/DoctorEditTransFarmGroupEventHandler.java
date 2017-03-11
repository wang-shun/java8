package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:47 17/3/10
 */

@Slf4j
@Component
public class DoctorEditTransFarmGroupEventHandler extends DoctorAbstractEditGroupEventHandler{
    @Override
    protected boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        return false;
    }

    @Override
    protected void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {
        DoctorTransFarmGroupEvent doctorTransFarmGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorTransFarmGroupEvent.class);
        if(Arguments.isNull(doctorTransFarmGroupEvent)) {
            log.error("parse doctorTransFarmGroupEvent faild, doctorGroupEvent = {}", doctorGroupEvent);
            throw new JsonResponseException("group.event.info.broken");
        }
        //更新track
        doctorGroupTrack.setQuantity(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupEvent.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(doctorGroupTrack.getBoarQty(), doctorTransFarmGroupEvent.getBoarQty());
        boarQty = boarQty > doctorGroupTrack.getQuantity() ? doctorGroupTrack.getQuantity() : boarQty;
        doctorGroupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        doctorGroupTrack.setSowQty(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupTrack.getBoarQty()));
    }
}
