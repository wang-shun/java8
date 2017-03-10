package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:00 17/3/10
 */

@Slf4j
@Component
public class DoctorEditChangeGroupEventHandler extends DoctorAbstractEditGroupEventHandler{

    @Override
    protected boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        if(doctorGroupTrack.getQuantity() < doctorGroupEvent.getQuantity()){
            return false;
        }
        return true;
    }

    @Override
    protected void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {

        DoctorChangeGroupEvent doctorChangeGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorChangeGroupEvent.class);
        if(Arguments.isNull(doctorChangeGroupEvent)) {
            log.error("parse doctorChangeGroupEvent faild, doctorGroupEvent = {}", doctorGroupEvent);
            throw new JsonResponseException("group.event.info.broken");
        }
        //更新track数量
        doctorGroupTrack.setQuantity(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupEvent.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(doctorGroupTrack.getBoarQty(), doctorChangeGroupEvent.getBoarQty());
        boarQty = boarQty > doctorGroupTrack.getQuantity() ? doctorGroupTrack.getQuantity() : boarQty;
        doctorGroupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        doctorGroupTrack.setSowQty(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupTrack.getBoarQty()));

        //母猪触发的变动，要减掉未断奶数
        if (doctorGroupEvent.getIsAuto() == IsOrNot.YES.getValue()) {
            if (doctorGroupTrack.getUnweanQty() == null || doctorGroupTrack.getUnweanQty() <= 0) {
                doctorGroupTrack.setUnweanQty(0);
            }
            doctorGroupTrack.setUnweanQty(doctorGroupTrack.getUnweanQty() - doctorGroupEvent.getQuantity());
        }
    }
}
