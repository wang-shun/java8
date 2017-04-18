package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;

/**
 * Created by xjn on 17/4/17.
 */
public class DoctorModifyGroupWeanEventHandler extends DoctorAbstractModifyGroupEventHandler
{
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorWeanGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorWeanGroupInput.class);
        DoctorWeanGroupInput newInput = (DoctorWeanGroupInput) input;
        return DoctorEventChangeDto.builder()
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .quantityChange(EventUtil.minusInt(newInput.getPartWeanPigletsCount(), oldInput.getPartWeanPigletsCount()))
                .groupUnqQtyChange(EventUtil.minusInt(oldInput.getPartWeanPigletsCount(), newInput.getPartWeanPigletsCount()))
                .weightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getPartWeanAvgWeight(), newInput.getPartWeanPigletsCount())
                        , EventUtil.getWeight(oldInput.getPartWeanAvgWeight(), oldInput.getPartWeanPigletsCount())))
                .build();
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorWeanGroupInput newInput = (DoctorWeanGroupInput) input;
        newEvent.setQuantity(newInput.getPartWeanPigletsCount());
        newEvent.setAvgWeight(newInput.getPartWeanAvgWeight());
        newEvent.setWeight(EventUtil.getWeight(newInput.getPartWeanAvgWeight(), newInput.getPartWeanPigletsCount()));
        return newEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setWeanQty(EventUtil.plusInt(oldGroupTrack.getWeanQty(), changeDto.getQuantityChange()));
        oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), changeDto.getGroupUnweanQtyChange()));
        oldGroupTrack.setWeanWeight(EventUtil.plusDouble(oldGroupTrack.getWeanWeight(), changeDto.getWeightChange()));
        return oldGroupTrack;
    }
}
