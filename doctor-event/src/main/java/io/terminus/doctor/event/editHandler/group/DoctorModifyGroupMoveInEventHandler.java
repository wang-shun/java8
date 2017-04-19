package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by xjn on 17/4/14.
 * 转入编辑和回滚
 */
@Slf4j
@Component
public class DoctorModifyGroupMoveInEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorMoveInGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorSowMoveInGroupInput.class);
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .farmId(oldGroupEvent.getFarmId())
                .businessId(oldGroupEvent.getGroupId())
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .build();
        if (notNull(oldGroupEvent.getSowId())) {
            changeDto.setGroupHealthyQtyChange(EventUtil.minusInt(newInput.getHealthyQty(), oldInput.getHealthyQty()));
            changeDto.setGroupWeakQtyChange(EventUtil.minusInt(newInput.getWeakQty(), oldInput.getWeakQty()));
            changeDto.setGroupLiveQtyChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()));
            changeDto.setGroupBirthWeightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getAvgWeight(), newInput.getQuantity()),
                    EventUtil.getAvgWeight(oldInput.getAvgWeight(), oldInput.getQuantity())));
            changeDto.setGroupUnweanQtyChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()));

        }
        return changeDto;
    }

    private DoctorEventChangeDto buildEventChange(DoctorGroupEvent groupEvent) {
        return buildEventChange(groupEvent, new DoctorMoveInGroupInput());
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setAvgWeight(newInput.getAvgWeight());
        newGroupEvent.setWeight(EventUtil.getWeight(newGroupEvent.getAvgWeight(), newGroupEvent.getQuantity()));
        newGroupEvent.setAvgDayAge(newInput.getAvgDayAge());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        oldGroupTrack.setBirthWeight(EventUtil.plusDouble(oldGroupTrack.getBirthWeight(), changeDto.getGroupBirthWeightChange()));
        oldGroupTrack.setLiveQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), changeDto.getGroupLiveQtyChange()));
        oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), changeDto.getGroupUnweanQtyChange()));
        oldGroupTrack.setHealthyQty(EventUtil.plusInt(oldGroupTrack.getHealthyQty(), changeDto.getGroupHealthyQtyChange()));
        oldGroupTrack.setWeakQty(EventUtil.plusInt(oldGroupTrack.getWeakQty(), changeDto.getGroupWeakQtyChange()));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup, changeDto, oldGroupEvent.getTransGroupType()));
        } else {
            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            DoctorMoveInGroupInput input1 = (DoctorMoveInGroupInput) input;
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder().quantityChange(input1.getQuantity()).build();
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, oldGroupEvent.getTransGroupType()));

            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            DoctorMoveInGroupInput input2 = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorMoveInGroupInput.class);
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder().quantityChange(EventUtil.minusInt(0, input2.getQuantity())).build();
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, oldGroupEvent.getTransGroupType()));
        }
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldTrack) {
        return buildNewTrack(oldTrack, buildEventChange(deleteGroupEvent));
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt());
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder().quantityChange(EventUtil.minusInt(0, deleteGroupEvent.getQuantity())).build();
        doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup, changeDto, deleteGroupEvent.getTransGroupType()));
    }

    /**
     * 构建日记录
     * @param oldDailyGroup 原记录
     * @param changeDto 变化量
     * @param tranType 转入类型
     * @return 新记录
     */
    private DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto, Integer tranType) {
        if (Objects.equals(tranType, DoctorGroupEvent.TransGroupType.OUT.getValue())) {
            oldDailyGroup.setOuterIn(EventUtil.plusInt(oldDailyGroup.getInnerIn(), changeDto.getQuantityChange()));
        } else {
            oldDailyGroup.setInnerIn(EventUtil.plusInt(oldDailyGroup.getInnerIn(), changeDto.getQuantityChange()));
        }
        oldDailyGroup.setUnweanCount(EventUtil.plusInt(oldDailyGroup.getUnweanCount(), changeDto.getGroupUnweanQtyChange()));
        oldDailyGroup.setEnd(EventUtil.plusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }
}
