package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/14.
 * 转入编辑和回滚
 */
@Slf4j
@Component
public class DoctorModifyMoveInEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorMoveInGroupInput oldMoveInGroupInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorSowMoveInGroupInput.class);
        DoctorMoveInGroupInput newMoveInGroupInput = (DoctorMoveInGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(newMoveInGroupInput.getQuantity(), oldMoveInGroupInput.getQuantity()))
                .groupHealthyQtyChange(EventUtil.minusInt(newMoveInGroupInput.getHealthyQty(), oldMoveInGroupInput.getHealthyQty()))
                .groupWeakQtyChange(EventUtil.minusInt(newMoveInGroupInput.getWeakQty(), oldMoveInGroupInput.getWeakQty()))
                .build();
        int oldLiveQty = EventUtil.plusInt(oldMoveInGroupInput.getHealthyQty(), oldMoveInGroupInput.getWeakQty());
        int newLiveQty = EventUtil.plusInt(newMoveInGroupInput.getHealthyQty(), newMoveInGroupInput.getWeakQty());
        changeDto.setLiveCountChange(newLiveQty - oldLiveQty);
        changeDto.setGroupBirthWeightChange(EventUtil.minusDouble(EventUtil.getWeight(newMoveInGroupInput.getAvgWeight(), newLiveQty),
                EventUtil.getAvgWeight(oldMoveInGroupInput.getAvgWeight(), oldLiveQty)));
        return changeDto;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = new DoctorGroupEvent();
        BeanMapper.copy(oldGroupEvent, newGroupEvent);
        createModifyLog(oldGroupEvent, newGroupEvent);
        DoctorMoveInGroupInput newMoveInGroupInput = (DoctorMoveInGroupInput) input;
        newGroupEvent.setExtra(TO_JSON_MAPPER.toJson(newMoveInGroupInput));
        newGroupEvent.setQuantity(newMoveInGroupInput.getQuantity());
        newGroupEvent.setAvgWeight(newMoveInGroupInput.getAvgWeight());
        newGroupEvent.setWeight(EventUtil.getWeight(newGroupEvent.getAvgWeight(), newGroupEvent.getQuantity()));
        newGroupEvent.setAvgDayAge(newMoveInGroupInput.getAvgDayAge());
        newGroupEvent.setRemark(newMoveInGroupInput.getRemark());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        oldGroupTrack.setBirthWeight(EventUtil.plusDouble(oldGroupTrack.getBirthWeight(), changeDto.getGroupBirthWeightChange()));
        oldGroupTrack.setLiveQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), changeDto.getGroupLiveQtyChange()));
        oldGroupTrack.setHealthyQty(EventUtil.plusInt(oldGroupTrack.getHealthyQty(), changeDto.getGroupHealthyQtyChange()));
        oldGroupTrack.setWeakQty(EventUtil.plusInt(oldGroupTrack.getWeakQty(), changeDto.getGroupWeakQtyChange()));
        return oldGroupTrack;
    }
}
