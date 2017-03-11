package io.terminus.doctor.event.editHandler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:29 17/3/10
 */

@Slf4j
@Component
public class DoctorEditTransGroupEventHandler extends DoctorAbstractEditGroupEventHandler{
    @Override
    protected boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        if(doctorGroupTrack.getQuantity() < doctorGroupEvent.getQuantity()){
            return false;
        }
        return true;
    }

    @Override
    protected void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {
        DoctorTransGroupEvent doctorTransGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorTransGroupEvent.class);
        if(Arguments.isNull(doctorTransGroupEvent)) {
            log.error("parse doctorTransGroupEvent faild, doctorGroupEvent = {}", doctorGroupEvent);
            throw new JsonResponseException("group.event.info.broken");
        }

        //更新quanity
        doctorGroupTrack.setQuantity(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupEvent.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(doctorGroupTrack.getBoarQty(), doctorTransGroupEvent.getBoarQty());
        boarQty = boarQty > doctorGroupTrack.getQuantity() ? doctorGroupTrack.getQuantity() : boarQty;
        doctorGroupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        doctorGroupTrack.setSowQty(EventUtil.minusQuantity(doctorGroupTrack.getQuantity(), doctorGroupTrack.getBoarQty()));

        //如果是母猪触发的转群事件，窝数-1，活仔，健仔数累减
        if (doctorGroupEvent.getIsAuto() == IsOrNot.YES.getValue()) {
            doctorGroupTrack.setNest(EventUtil.plusInt(doctorGroupTrack.getNest(), -1));
            doctorGroupTrack.setLiveQty(EventUtil.plusInt(doctorGroupTrack.getLiveQty(), - doctorGroupEvent.getQuantity()));
            doctorGroupTrack.setHealthyQty(doctorGroupTrack.getLiveQty() - MoreObjects.firstNonNull(doctorGroupTrack.getWeakQty(), 0));
            doctorGroupTrack.setUnweanQty(EventUtil.plusInt(doctorGroupTrack.getUnweanQty(), -doctorGroupEvent.getQuantity()));
            doctorGroupTrack.setBirthWeight(EventUtil.plusDouble(doctorGroupTrack.getBirthWeight(), - doctorGroupEvent.getAvgWeight() * doctorGroupEvent.getQuantity()));
        }
    }
}
