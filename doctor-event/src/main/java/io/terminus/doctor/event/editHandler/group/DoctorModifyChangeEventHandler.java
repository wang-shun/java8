package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 14:01 2017/4/15
 */

public class DoctorModifyChangeEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .oldEventAt(DateTime.parse(newInput.getEventAt()).toDate())
                .oldChangeTypeId(oldInput.getChangeTypeId())
                .oldChangeReasonId(oldInput.getChangeReasonId())
                .changeTypeId(newInput.getChangeTypeId())
                .changeReasonId(newInput.getChangeReasonId())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .weightChange(EventUtil.minusDouble(newInput.getWeight(), oldInput.getWeight()))
                .priceChange(EventUtil.minusLong(newInput.getPrice(), oldInput.getPrice()))
                .overPriceChange(EventUtil.minusLong(newInput.getOverPrice(), oldInput.getOverPrice()))
                .build();
        return changeDto;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = new DoctorGroupEvent();
        BeanMapper.copy(oldGroupEvent, newGroupEvent);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        newGroupEvent.setEventAt(DateTime.parse(newInput.getEventAt()).toDate());
        newGroupEvent.setChangeTypeId(newInput.getChangeTypeId());
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setWeight(newInput.getWeight());
        newGroupEvent.setPrice(newInput.getPrice());
        newGroupEvent.setOverPrice(newInput.getOverPrice());
        newGroupEvent.setExtra(TO_JSON_MAPPER.toJson(newInput));
        newGroupEvent.setRemark(newInput.getRemark());
        return newGroupEvent;
    }
}
