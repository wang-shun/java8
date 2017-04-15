package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.*;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 16:10 2017/4/15
 */

public class DoctorModifyTransFarmEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTransFarmGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTransFarmGroupInput.class);
        DoctorTransFarmGroupInput newInput = (DoctorTransFarmGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .oldEventAt(DateTime.parse(oldInput.getEventAt()).toDate())
                .newEventAt(DateTime.parse(newInput.getEventAt()).toDate())
                .oldToFarmId(oldInput.getToFarmId())
                .toFarmId(newInput.getToFarmId())
                .oldToGroupId(oldGroupEvent.getGroupId())
                .toGroupId(newInput.getToGroupId())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .build();
        return changeDto;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        return null;
    }
}
