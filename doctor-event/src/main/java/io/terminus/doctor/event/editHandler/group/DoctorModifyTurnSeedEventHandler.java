package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.joda.time.DateTime;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 10:45 2017/4/15
 */

public class DoctorModifyTurnSeedEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTurnSeedGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTurnSeedGroupInput.class);
        DoctorTurnSeedGroupInput newInput = (DoctorTurnSeedGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .oldEventAt(DateTime.parse(oldInput.getEventAt()).toDate())
                .newEventAt(DateTime.parse(newInput.getEventAt()).toDate())
                .oldPigCode(oldInput.getPigCode())
                .pigCode(newInput.getPigCode())
                .weightChange(newInput.getWeight() - oldInput.getWeight())
                .build();
        return changeDto;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = new DoctorGroupEvent();
        BeanMapper.copy(oldGroupEvent, newGroupEvent);
        DoctorTurnSeedGroupInput newInput = (DoctorTurnSeedGroupInput) input;
        newGroupEvent.setExtra(TO_JSON_MAPPER.toJson(newInput));
        newGroupEvent.setEventAt(DateTime.parse(newInput.getEventAt()).toDate());
        newGroupEvent.setRemark(newInput.getRemark());
        newGroupEvent.setDesc(newInput.generateEventDesc());
        return newGroupEvent;
    }
}
