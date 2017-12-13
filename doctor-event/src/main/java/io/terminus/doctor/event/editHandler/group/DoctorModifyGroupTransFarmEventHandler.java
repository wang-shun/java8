package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 16:10 2017/4/15
 */
@Component
public class DoctorModifyGroupTransFarmEventHandler extends DoctorAbstractModifyGroupEventHandler{


    @Autowired
    private DoctorModifyGroupMoveInEventHandler modifyGroupMoveInEventHandler;
    @Autowired
    private DoctorModifyGroupNewEventHandler modifyGroupNewEventHandler;
    @Autowired
    private DoctorModifyGroupCloseEventHandler modifyGroupCloseEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        throw new InvalidException("chg.farm.not.allow.modify");
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorTransFarmGroupInput newInput = (DoctorTransFarmGroupInput) input;
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setWeight(newInput.getWeight());
        newGroupEvent.setAvgWeight(EventUtil.getAvgWeight(newGroupEvent.getWeight(), newGroupEvent.getQuantity()));
        return newGroupEvent;
    }

    @Override
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.MOVE_IN.getValue());
        Boolean isRollback = modifyGroupMoveInEventHandler.rollbackHandleCheck(moveInEvent);

        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            isRollback &= modifyGroupNewEventHandler.rollbackHandleCheck(newCreateEvent);
        }

        //关闭事件回滚
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            isRollback &= modifyGroupCloseEventHandler.rollbackHandleCheck(closeEvent);
        }

        return isRollback;
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
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), deleteGroupEvent.getQuantity()));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        updateDailyOfDelete(deleteGroupEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {
        DoctorTransFarmGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTransFarmGroupInput.class);
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                .weightChange(EventUtil.minusDouble(0D, oldInput.getWeight()))
                .build();
        DoctorGroupDaily oldDailyGroup1 = doctorGroupDailyDao.findBy(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(), oldGroupEvent.getEventAt());
        doctorGroupDailyDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(),
                getAfterDay(oldGroupEvent.getEventAt()), -changeDto1.getQuantityChange());

    }

    @Override
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {
        Date eventAt = DateUtil.toDate(input.getEventAt());
        DoctorTransFarmGroupInput newInput = (DoctorTransFarmGroupInput) input;
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .quantityChange(newInput.getQuantity())
                .weightChange(newInput.getWeight())
                .build();
        DoctorGroupDaily oldDailyGroup2 = doctorGroupDailyDao.findBy(newGroupEvent.getFarmId(), newGroupEvent.getPigType(), eventAt);
        doctorDailyReportManager.createOrUpdateGroupDaily(buildDailyGroup(oldDailyGroup2, changeDto2));
        updateDailyGroupLiveStock(newGroupEvent.getFarmId(), newGroupEvent.getPigType(),
                getAfterDay(eventAt), -changeDto2.getQuantityChange());
    }

    @Override
    protected DoctorGroupDaily buildDailyGroup(DoctorGroupDaily oldDailyGroup, DoctorEventChangeDto changeDto) {
        oldDailyGroup = super.buildDailyGroup(oldDailyGroup, changeDto);
        oldDailyGroup.setChgFarm(EventUtil.plusInt(oldDailyGroup.getChgFarm(), changeDto.getQuantityChange()));
        oldDailyGroup.setChgFarmWeight(EventUtil.plusDouble(oldDailyGroup.getChgFarmWeight(), changeDto.getWeightChange()));
        oldDailyGroup.setTurnOutWeight(EventUtil.plusDouble(oldDailyGroup.getTurnOutWeight(), changeDto.getWeightChange()));
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }
}
