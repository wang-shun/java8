package io.terminus.doctor.event.editHandler.group;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
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
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        super.modifyHandleCheck(oldGroupEvent, input);
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
        validGroupLiveStock(oldGroupEvent.getGroupId(), oldGroupEvent.getGroupCode(),
                oldGroupEvent.getEventAt(), DateUtil.toDate(newInput.getEventAt()),
                -oldGroupEvent.getQuantity(), newInput.getQuantity(),
                EventUtil.minusInt(newInput.getQuantity(), oldGroupEvent.getQuantity()));
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorMoveInGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorSowMoveInGroupInput.class);
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
        DoctorEventChangeDto changeDto = buildEventChange(oldInput, newInput);
        changeDto.setFarmId(oldGroupEvent.getFarmId());
        changeDto.setPigType(oldGroupEvent.getPigType());
        changeDto.setBusinessId(oldGroupEvent.getGroupId());
        changeDto.setIsSowTrigger(notNull(oldGroupEvent.getSowId()));
        changeDto.setIsFarrowIn(notNull(oldGroupEvent.getRelPigEventId()));
        changeDto.setTransGroupType(oldGroupEvent.getTransGroupType());
        changeDto.setInType(oldGroupEvent.getInType());
        changeDto.setWeightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getAvgWeight(), newInput.getQuantity()),
                EventUtil.getWeight(oldInput.getAvgWeight(), oldInput.getQuantity())));
        changeDto.setAgeChange(EventUtil.minusInt(EventUtil.get(newInput.getAvgDayAge(), newInput.getQuantity()),
                EventUtil.get(oldInput.getAvgDayAge(), oldInput.getQuantity())));

        //母猪触发
        if (notNull(oldGroupEvent.getSowId())) {
            changeDto.setGroupHealthyQtyChange(EventUtil.minusInt(newInput.getHealthyQty(), oldInput.getHealthyQty()));
            changeDto.setGroupWeakQtyChange(EventUtil.minusInt(newInput.getWeakQty(), oldInput.getWeakQty()));
            changeDto.setGroupBirthWeightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getAvgWeight(), newInput.getQuantity()),
                    EventUtil.getWeight(oldInput.getAvgWeight(), oldInput.getQuantity())));
        }
        return changeDto;
    }

    private DoctorEventChangeDto buildEventChange(BaseGroupInput oldInputDto, BaseGroupInput newInputDto) {
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) newInputDto;
        DoctorMoveInGroupInput oldInput = (DoctorMoveInGroupInput) oldInputDto;
        return DoctorEventChangeDto.builder()
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .build();
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorMoveInGroupInput newInput = (DoctorMoveInGroupInput) input;
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setAvgWeight(newInput.getAvgWeight());
        newGroupEvent.setWeight(EventUtil.getWeight(newGroupEvent.getAvgWeight(), newGroupEvent.getQuantity()));
        newGroupEvent.setAvgDayAge(newInput.getAvgDayAge());
        newGroupEvent.setAmount(newInput.getAmount());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        //母猪触发
        if (changeDto.getIsSowTrigger()) {
            oldGroupTrack.setBirthWeight(EventUtil.plusDouble(oldGroupTrack.getBirthWeight(), changeDto.getGroupBirthWeightChange()));
            oldGroupTrack.setLiveQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setHealthyQty(EventUtil.plusInt(oldGroupTrack.getHealthyQty(), changeDto.getGroupHealthyQtyChange()));
            oldGroupTrack.setWeakQty(EventUtil.plusInt(oldGroupTrack.getWeakQty(), changeDto.getGroupWeakQtyChange()));
        }

        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        oldGroupTrack.setBirthDate(getAvgDay(oldGroupTrack.getGroupId()));
        oldGroupTrack.setAvgDayAge(DateUtil.getDeltaDays(oldGroupTrack.getBirthDate(), new Date()));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        if (Objects.equals(oldGroupEvent.getTransGroupType(), DoctorGroupEvent.TransGroupType.IN.getValue())) {
            return;
        }
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorGroupDaily oldDailyGroup = doctorDailyReportManager.findDoctorGroupDaily(changeDto.getFarmId(), changeDto.getPigType(), changeDto.getOldEventAt());
            buildDailyGroup(oldDailyGroup, changeDto);
            doctorGroupDailyDao.update(oldDailyGroup);
            updateDailyGroupLiveStock(changeDto.getFarmId(), changeDto.getPigType(), getAfterDay(changeDto.getOldEventAt()), changeDto.getQuantityChange());
            return;
        }
        updateDailyOfDelete(oldGroupEvent);
        updateDailyOfNew(oldGroupEvent, input);
    }

    @Override
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        return validGroupLiveStockForDelete(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt(), -deleteGroupEvent.getQuantity());
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        if (notNull(deleteGroupEvent.getSowId())) {
            oldGroupTrack.setNest(EventUtil.minusInt(oldGroupTrack.getNest(), 1));
        }
        oldGroupTrack.setBirthDate(getAvgDay(oldGroupTrack.getGroupId()));
        oldGroupTrack.setAvgDayAge(DateUtil.getDeltaDays(oldGroupTrack.getBirthDate(), new Date()));
        return buildNewTrack(oldGroupTrack, buildEventChange(deleteGroupEvent, new DoctorMoveInGroupInput()));
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        updateDailyOfDelete(deleteGroupEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {
        if (Objects.equals(oldGroupEvent.getTransGroupType(), DoctorGroupEvent.TransGroupType.IN.getValue())) {
            return;
        }
        DoctorMoveInGroupInput input1 = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorMoveInGroupInput.class);
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, input1.getQuantity()))
                .weightChange(EventUtil.minusDouble(0.0, EventUtil.getWeight(input1.getAvgWeight(), input1.getQuantity())))
                .ageChange(EventUtil.minusInt(0, EventUtil.get(input1.getAvgDayAge(), input1.getQuantity())))
                .transGroupType(oldGroupEvent.getTransGroupType())
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                .isFarrowIn(notNull(oldGroupEvent.getRelPigEventId()))
                .inType(oldGroupEvent.getInType())
                .build();
        DoctorGroupDaily oldDailyGroup1 = doctorDailyReportManager.findDoctorGroupDaily(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(), oldGroupEvent.getEventAt());
        doctorGroupDailyDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(), getAfterDay(oldGroupEvent.getEventAt()), changeDto1.getQuantityChange());

    }

    @Override
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {
        if (Objects.equals(newGroupEvent.getTransGroupType(), DoctorGroupEvent.TransGroupType.IN.getValue())) {
            return;
        }
        Date eventAt = DateUtil.toDate(input.getEventAt());
        DoctorMoveInGroupInput input2 = (DoctorMoveInGroupInput) input;
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .quantityChange(input2.getQuantity())
                .quantityChange(EventUtil.get(input2.getAvgDayAge(), input2.getQuantity()))
                .weightChange(EventUtil.getWeight(input2.getAvgWeight(), input2.getQuantity()))
                .transGroupType(newGroupEvent.getTransGroupType())
                .isSowTrigger(notNull(newGroupEvent.getSowId()))
                .isFarrowIn(notNull(newGroupEvent.getRelPigEventId()))
                .inType(newGroupEvent.getInType())
                .build();
        DoctorGroupDaily oldDailyGroup2 = doctorDailyReportManager.findDoctorGroupDaily(newGroupEvent.getFarmId(), newGroupEvent.getPigType(), eventAt);
        doctorDailyReportManager.createOrUpdateGroupDaily(buildDailyGroup(oldDailyGroup2, changeDto2));
        updateDailyGroupLiveStock(newGroupEvent.getFarmId(), newGroupEvent.getPigType(), getAfterDay(eventAt), changeDto2.getQuantityChange());

    }

    /**
     * 构建日记录
     *
     * @param oldDailyGroup 原记录
     * @param changeDto     变化量
     * @return 新记录
     */
    @Override
    protected DoctorGroupDaily buildDailyGroup(DoctorGroupDaily oldDailyGroup, DoctorEventChangeDto changeDto) {
        oldDailyGroup = super.buildDailyGroup(oldDailyGroup, changeDto);
        oldDailyGroup.setTurnInto(EventUtil.plusInt(oldDailyGroup.getTurnInto(), changeDto.getQuantityChange()));
        oldDailyGroup.setTurnIntoWeight(EventUtil.plusDouble(oldDailyGroup.getTurnIntoWeight(), changeDto.getWeightChange()));
        oldDailyGroup.setTurnIntoAge(EventUtil.plusInt(oldDailyGroup.getTurnIntoAge(), changeDto.getAgeChange()));
        oldDailyGroup.setEnd(EventUtil.plusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        if (Objects.equals(changeDto.getInType(), InType.FARM.getValue())) {
            oldDailyGroup.setChgFarmIn(EventUtil.plusInt(oldDailyGroup.getChgFarmIn(), changeDto.getQuantityChange()));
        }
        return oldDailyGroup;
    }

    /**
     * 获取转移类型
     * @param moveInEvent 转入事件
     * @return 转入类型
     */
    private Integer getTransGroupType(DoctorGroupEvent moveInEvent) {
        return !Objects.equals(moveInEvent.getInType(), InType.GROUP.getValue()) ?
                DoctorGroupEvent.TransGroupType.OUT.getValue() : moveInEvent.getTransGroupType();
    }

    /**
     * 获取猪群的初始日期用于计算日龄
     * @param groupId 猪群id
     * @return 日龄
     */
    public Date getAvgDay(Long groupId) {
        List<Integer> includeTypes = Lists.newArrayList(GroupEventType.CHANGE.getValue(), GroupEventType.MOVE_IN.getValue(),
                GroupEventType.TRANS_FARM.getValue(), GroupEventType.TRANS_GROUP.getValue());
        List<DoctorGroupEvent> groupEventList = doctorGroupEventDao.findEventIncludeTypes(groupId, includeTypes);
        int currentQuantity = 0;
        int avgDay = 0;
        Date lastEvent = new Date();
        for (DoctorGroupEvent groupEvent : groupEventList) {
            if (Objects.equals(MoreObjects.firstNonNull(groupEvent.getQuantity(), 0), 0)) {
                continue;
            }
            if (Objects.equals(groupEvent.getType(), GroupEventType.MOVE_IN.getValue())) {
                avgDay = avgDay + DateUtil.getDeltaDays(lastEvent, groupEvent.getEventAt());
                avgDay = EventUtil.getAvgDayAge(avgDay, currentQuantity, groupEvent.getAvgDayAge(), groupEvent.getQuantity());
                currentQuantity += groupEvent.getQuantity();
                lastEvent = groupEvent.getEventAt();
            } else {
                currentQuantity -= groupEvent.getQuantity();
            }
        }
        return new DateTime(lastEvent).minusDays(avgDay).toDate();
    }
}
