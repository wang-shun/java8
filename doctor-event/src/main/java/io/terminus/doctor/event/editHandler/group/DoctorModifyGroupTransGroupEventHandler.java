package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;


/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:31 2017/4/15
 */
@Component
public class DoctorModifyGroupTransGroupEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Autowired
    private DoctorModifyGroupMoveInEventHandler modifyGroupMoveInEventHandler;
    @Autowired
    private DoctorModifyGroupNewEventHandler modifyGroupNewEventHandler;
    @Autowired
    private DoctorModifyGroupCloseEventHandler modifyGroupCloseEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        super.modifyHandleCheck(oldGroupEvent, input);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        validGroupLiveStock(oldGroupEvent.getGroupId(), oldGroupEvent.getGroupCode(),
                oldGroupEvent.getEventAt(), DateUtil.toDate(newInput.getEventAt()),
                oldGroupEvent.getQuantity(), newInput.getQuantity());
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTransGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTransGroupInput.class);
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        return DoctorEventChangeDto.builder()
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
                .transGroupType(oldGroupEvent.getTransGroupType())
                .build();
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
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup, changeDto));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(oldGroupEvent.getEventAt()), EventUtil.minusInt(0, changeDto.getQuantityChange()));
        } else {
            updateDailyOfDelete(oldGroupEvent);
            updateDailyOfNew(oldGroupEvent, input);
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorGroupEvent newEvent) {
        //1.新建事件编辑
        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(newEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            BaseGroupInput newInput = JSON_MAPPER.fromJson(newCreateEvent.getExtra(), DoctorNewGroupInput.class);
            newInput.setEventAt(DateUtil.toDateString(newEvent.getEventAt()));
            modifyGroupNewEventHandler.modifyHandle(newCreateEvent, newInput);
        }
        //2.转入事件编辑
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(newEvent.getId(), GroupEventType.MOVE_IN.getValue());
        modifyGroupMoveInEventHandler.modifyHandle(moveInEvent, buildTriggerGroupEventInput(newEvent));

        //3.关闭事件编辑
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(newEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            modifyGroupCloseEventHandler.modifyHandle(closeEvent, buildGroupCloseInput(newEvent));
        }
    }

    @Override
    protected Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        return validGroupLiveStock(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt(), -deleteGroupEvent.getQuantity());
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {
        //1.转入回滚
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.MOVE_IN.getValue());
        modifyGroupMoveInEventHandler.rollbackHandle(moveInEvent, operatorId, operatorName);

        //2.新建回滚
        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            modifyGroupNewEventHandler.rollbackHandle(newCreateEvent, operatorId, operatorName);
        }

        //3.关闭回滚
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            modifyGroupCloseEventHandler.rollbackHandle(closeEvent, operatorId, operatorName);
        }
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
        updateDailyOfDelete(deleteGroupEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {
        DoctorMoveInGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorMoveInGroupInput.class);
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                .transGroupType(oldGroupEvent.getTransGroupType())
                .build();
        DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(oldGroupEvent.getGroupId(), oldGroupEvent.getEventAt());
        doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(oldGroupEvent.getGroupId(), getAfterDay(oldGroupEvent.getEventAt()), -changeDto1.getQuantityChange());
    }

    @Override
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {
        Date eventAt = DateUtil.toDate(input.getEventAt());
        DoctorTransGroupInput newInput = (DoctorTransGroupInput) input;
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .quantityChange(newInput.getQuantity())
                .isSowTrigger(notNull(newGroupEvent.getSowId()))
                .transGroupType(newGroupEvent.getTransGroupType())
                .build();
        DoctorDailyGroup oldDailyGroup2 = doctorDailyReportManager.findByGroupIdAndSumAt(newGroupEvent.getGroupId(), eventAt);
        doctorDailyReportManager.createOrUpdateDailyGroup(buildDailyGroup(oldDailyGroup2, changeDto2));
        updateDailyGroupLiveStock(newGroupEvent.getGroupId(), getAfterDay(eventAt), -changeDto2.getQuantityChange());

    }

    @Override
    protected DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getTransGroupType(), DoctorGroupEvent.TransGroupType.OUT.getValue())) {
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

    /**
     * 构建触发猪群事件输入
     * @param transGroupEvent 原事件
     * @return 触发事件输入
     */
    public DoctorMoveInGroupInput buildTriggerGroupEventInput(DoctorGroupEvent transGroupEvent) {
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

        moveIn.setInType(InType.GROUP.getValue());       //转入类型
        moveIn.setInTypeName(InType.GROUP.getDesc());
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
