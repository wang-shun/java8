package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by xjn on 17/4/17.
 */
@Component
public class DoctorModifyGroupWeanEventHandler extends DoctorAbstractModifyGroupEventHandler
{
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorWeanGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorWeanGroupInput.class);
        DoctorWeanGroupInput newInput = (DoctorWeanGroupInput) input;
        return DoctorEventChangeDto.builder()
                .farmId(oldGroupEvent.getFarmId())
                .businessId(oldGroupEvent.getGroupId())
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .quantityChange(EventUtil.minusInt(newInput.getPartWeanPigletsCount(), oldInput.getPartWeanPigletsCount()))
                .weightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getPartWeanAvgWeight(), newInput.getPartWeanPigletsCount())
                        , EventUtil.getWeight(oldInput.getPartWeanAvgWeight(), oldInput.getPartWeanPigletsCount())))
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
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
        oldGroupTrack.setUnweanQty(EventUtil.minusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
        oldGroupTrack.setWeanQty(EventUtil.plusInt(oldGroupTrack.getWeanQty(), changeDto.getQuantityChange()));
        oldGroupTrack.setWeanWeight(EventUtil.plusDouble(oldGroupTrack.getWeanWeight(), changeDto.getWeightChange()));
        return oldGroupTrack;
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), deleteGroupEvent.getQuantity()));
        oldGroupTrack.setWeanQty(EventUtil.minusInt(oldGroupTrack.getWeanQty(), deleteGroupEvent.getQuantity()));
        oldGroupTrack.setWeanWeight(EventUtil.minusDouble(oldGroupTrack.getWeanWeight(), deleteGroupEvent.getWeight()));
        return oldGroupTrack;
    }
}
