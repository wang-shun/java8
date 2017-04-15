package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;


/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:31 2017/4/15
 */

public class DoctorModifyTransGroupEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTransGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTransGroupInput.class);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .oldEventAt(DateTime.parse(oldInput.getEventAt()).toDate())
                .newEventAt(DateTime.parse(newInput.getEventAt()).toDate())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .avgWeightChange(EventUtil.minusDouble(newInput.getAvgWeight(), oldInput.getAvgWeight()))
                .build();
        return changeDto;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = new DoctorGroupEvent();
        BeanMapper.copy(oldGroupEvent, newGroupEvent);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        newGroupEvent.setEventAt(DateTime.parse(newInput.getEventAt()).toDate());
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setAvgWeight(newInput.getAvgWeight());
        newGroupEvent.setRemark(newInput.getRemark());
        return newGroupEvent;
    }
}
