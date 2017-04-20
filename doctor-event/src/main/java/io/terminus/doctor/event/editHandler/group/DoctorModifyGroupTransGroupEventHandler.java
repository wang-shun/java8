package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;


/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:31 2017/4/15
 */

public class DoctorModifyGroupTransGroupEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Autowired
    private DoctorModifyGroupMoveInEventHandler doctorModifyGroupMoveInEventHandler;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTransGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTransGroupInput.class);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .farmId(oldGroupEvent.getFarmId())
                .businessId(oldGroupEvent.getGroupId())
                .oldToGroupId(oldInput.getToGroupId())
                .toGroupId(newInput.getToGroupId())
                .oldEventAt(DateTime.parse(oldInput.getEventAt()).toDate())
                .newEventAt(DateTime.parse(newInput.getEventAt()).toDate())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .avgWeightChange(EventUtil.minusDouble(newInput.getAvgWeight(), oldInput.getAvgWeight()))
                .weightChange(EventUtil.minusDouble(newInput.getWeight(), oldInput.getWeight()))
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                .build();
        return changeDto;
    }

    @Override
    protected DoctorEventChangeDto buildEventChange(BaseGroupInput oldInputDto, BaseGroupInput newInputDto) {
        DoctorTransGroupInput oldInput = (DoctorTransGroupInput) oldInputDto;
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) newInputDto;
        return DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .avgWeightChange(EventUtil.minusDouble(newInput.getAvgWeight(), oldInput.getAvgWeight()))
                .weightChange(EventUtil.minusDouble(newInput.getWeight(), oldInput.getWeight()))
                .build();
    }

    @Override
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto) {
        super.modifyHandleCheck(oldGroupEvent, changeDto);
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setWeight(newInput.getWeight());
        newGroupEvent.setAvgWeight(newInput.getAvgWeight());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        if (changeDto.getIsSowTrigger()) {
            oldGroupTrack.setLiveQty(EventUtil.minusInt(oldGroupTrack.getLiveQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setHealthyQty(EventUtil.minusInt(oldGroupTrack.getLiveQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setBirthWeight(EventUtil.minusDouble(oldGroupTrack.getBirthWeight(), changeDto.getWeightChange()));
            oldGroupTrack.setUnweanQty(EventUtil.minusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        oldGroupTrack.setQuantity(EventUtil.minusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup, changeDto, oldGroupEvent.getTransGroupType()));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getOldEventAt()).plusDays(1).toDate(), EventUtil.minusInt(0, changeDto.getQuantityChange()));
        } else {
            DoctorMoveInGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorMoveInGroupInput.class);
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .quantityChange(newInput.getQuantity())
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();
            updateDailyWhenEventDiff(changeDto, changeDto1, changeDto2, oldGroupEvent.getTransGroupType());
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorGroupEvent newEvent) {
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(newEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyGroupMoveInEventHandler.modifyHandle(moveInEvent, buildTriggerEventInput(newEvent));
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyGroupMoveInEventHandler.rollbackHandle(moveInEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        if (notNull(deleteGroupEvent)) {
            oldGroupTrack.setLiveQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), deleteGroupEvent.getQuantity()));
            oldGroupTrack.setHealthyQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), deleteGroupEvent.getQuantity()));
            oldGroupTrack.setBirthWeight(EventUtil.plusDouble(oldGroupTrack.getBirthWeight(), deleteGroupEvent.getWeight()));
            oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), deleteGroupEvent.getQuantity()));
        }
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), deleteGroupEvent.getQuantity()));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt());
        DoctorTransGroupInput oldInput = JSON_MAPPER.fromJson(deleteGroupEvent.getExtra(), DoctorTransGroupInput.class);
        buildDailyGroup(oldDailyGroup, buildEventChange(oldInput, new DoctorTransGroupInput()), deleteGroupEvent.getTransGroupType());
        updateDailyGroupLiveStock(deleteGroupEvent.getGroupId(), getAfterDay(deleteGroupEvent.getEventAt()), deleteGroupEvent.getQuantity());
    }

    private void updateDailyWhenEventDiff(DoctorEventChangeDto changeDto, DoctorEventChangeDto changeDto1, DoctorEventChangeDto changeDto2, Integer transGroupType) {
        if (changeDto.getOldEventAt().before(changeDto.getNewEventAt())) {
            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), changeDto.getOldEventAt(), -changeDto1.getQuantityChange());

            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), changeDto.getNewEventAt(), -changeDto2.getQuantityChange());
        } else {
            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), changeDto.getNewEventAt(), -changeDto2.getQuantityChange());

            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), changeDto.getOldEventAt(), -changeDto1.getQuantityChange());
        }
    }

    private DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto, Integer tranGroupType) {
        if (Objects.equals(tranGroupType, DoctorGroupEvent.TransGroupType.OUT.getValue())) {
            oldDailyGroup.setOuterOut(EventUtil.plusInt(oldDailyGroup.getOuterOut(), changeDto.getQuantityChange()));
        } else {
            oldDailyGroup.setInnerOut(EventUtil.plusInt(oldDailyGroup.getInnerOut(), changeDto.getQuantityChange()));
        }
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));

        if (changeDto.getIsSowTrigger()) {
            oldDailyGroup.setUnweanCount(EventUtil.minusInt(oldDailyGroup.getUnweanCount(), changeDto.getQuantityChange()));
        }
        return oldDailyGroup;
    }

    private DoctorMoveInGroupInput buildTriggerEventInput(DoctorGroupEvent transGroupEvent) {
        DoctorTransGroupInput transGroup = JSON_MAPPER.fromJson(transGroupEvent.getExtra(), DoctorTransGroupInput.class);
        DoctorMoveInGroupInput moveIn = new DoctorMoveInGroupInput();
        moveIn.setSowId(transGroup.getSowId());
        moveIn.setSowCode(transGroup.getSowCode());
        moveIn.setEventAt(transGroup.getEventAt());
        moveIn.setEventType(GroupEventType.MOVE_IN.getValue());
        moveIn.setIsAuto(IsOrNot.YES.getValue());
        moveIn.setCreatorId(transGroup.getCreatorId());
        moveIn.setCreatorName(transGroup.getCreatorName());
        moveIn.setRelGroupEventId(transGroup.getRelGroupEventId());

        moveIn.setInType(DoctorMoveInGroupEvent.InType.GROUP.getValue());       //转入类型
        moveIn.setInTypeName(DoctorMoveInGroupEvent.InType.GROUP.getDesc());
        moveIn.setSource(transGroup.getSource());                 //来源可以分为 本场(转群), 外场(转场)
//        moveIn.setSex(fromGroupTrack.getSex());
        moveIn.setBreedId(transGroup.getBreedId());
        moveIn.setBreedName(transGroup.getBreedName());
        moveIn.setFromBarnId(transGroupEvent.getBarnId());         //来源猪舍
        moveIn.setFromBarnName(transGroupEvent.getBarnName());
        moveIn.setFromGroupId(transGroupEvent.getGroupId());                   //来源猪群
        moveIn.setFromGroupCode(transGroupEvent.getGroupCode());
        moveIn.setQuantity(transGroup.getQuantity());
        moveIn.setBoarQty(transGroup.getBoarQty());
        moveIn.setSowQty(transGroup.getSowQty());
//        moveIn.setAvgDayAge(fromGroupTrack.getAvgDayAge());     //日龄
        moveIn.setAvgWeight(EventUtil.getAvgWeight(transGroup.getWeight(), transGroup.getQuantity()));  //转入均重
        moveIn.setSowEvent(transGroup.isSowEvent());    //是否是由母猪触发的转入
        return moveIn;
    }
}
