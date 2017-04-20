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
import org.joda.time.DateTime;
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
        DoctorEventChangeDto changeDto = buildEventChange(oldInput, newInput);
        changeDto.setFarmId(oldGroupEvent.getFarmId());
        changeDto.setBusinessId(oldGroupEvent.getGroupId());
        changeDto.setIsSowTrigger(notNull(oldGroupEvent.getSowId()));
        if (notNull(oldGroupEvent.getSowId())) {
            changeDto.setGroupHealthyQtyChange(EventUtil.minusInt(newInput.getHealthyQty(), oldInput.getHealthyQty()));
            changeDto.setGroupWeakQtyChange(EventUtil.minusInt(newInput.getWeakQty(), oldInput.getWeakQty()));
            changeDto.setGroupBirthWeightChange(EventUtil.minusDouble(EventUtil.getWeight(newInput.getAvgWeight(), newInput.getQuantity()),
                    EventUtil.getAvgWeight(oldInput.getAvgWeight(), oldInput.getQuantity())));

        }
        return changeDto;
    }

    @Override
    protected DoctorEventChangeDto buildEventChange(BaseGroupInput oldInputDto, BaseGroupInput newInputDto) {
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
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        if (changeDto.getIsSowTrigger()) {
            oldGroupTrack.setBirthWeight(EventUtil.plusDouble(oldGroupTrack.getBirthWeight(), changeDto.getGroupBirthWeightChange()));
            oldGroupTrack.setLiveQty(EventUtil.plusInt(oldGroupTrack.getLiveQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
            oldGroupTrack.setHealthyQty(EventUtil.plusInt(oldGroupTrack.getHealthyQty(), changeDto.getGroupHealthyQtyChange()));
            oldGroupTrack.setWeakQty(EventUtil.plusInt(oldGroupTrack.getWeakQty(), changeDto.getGroupWeakQtyChange()));

        }
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {

        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            buildDailyGroup(oldDailyGroup, changeDto, oldGroupEvent.getTransGroupType());
            doctorDailyGroupDao.update(oldDailyGroup);
            updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getNewEventAt()).plusDays(1).toDate(), changeDto.getQuantityChange());
        } else {
            DoctorMoveInGroupInput input1 = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorMoveInGroupInput.class);
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .quantityChange(EventUtil.minusInt(0, input1.getQuantity()))
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            DoctorMoveInGroupInput input2 = (DoctorMoveInGroupInput) input;
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .quantityChange(input2.getQuantity())
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();
            updateDailyWhenEventDiff(changeDto, changeDto1, changeDto2, oldGroupEvent.getTransGroupType());
        }
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        if (notNull(deleteGroupEvent.getSowId())) {
            oldGroupTrack.setNest(EventUtil.minusInt(oldGroupTrack.getNest(), 1));
        }
        return buildNewTrack(oldGroupTrack, buildEventChange(deleteGroupEvent, new DoctorMoveInGroupInput()));
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt());
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, deleteGroupEvent.getQuantity()))
                .isSowTrigger(notNull(deleteGroupEvent.getSowId()))
                .build();
        doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup, changeDto, deleteGroupEvent.getTransGroupType()));
        updateDailyGroupLiveStock(deleteGroupEvent.getGroupId(), new DateTime(deleteGroupEvent.getEventAt()).plusDays(1).toDate(), changeDto.getQuantityChange());
    }

    private void updateDailyWhenEventDiff(DoctorEventChangeDto changeDto, DoctorEventChangeDto changeDto1, DoctorEventChangeDto changeDto2, Integer transGroupType) {
        if (changeDto.getOldEventAt().before(changeDto.getNewEventAt())) {
            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getOldEventAt()), changeDto1.getQuantityChange());

            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getNewEventAt()), changeDto2.getQuantityChange());
        } else {
            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getNewEventAt()), changeDto2.getQuantityChange());

            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, transGroupType));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getOldEventAt()), changeDto1.getQuantityChange());
        }
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
            oldDailyGroup.setOuterIn(EventUtil.plusInt(oldDailyGroup.getOuterIn(), changeDto.getQuantityChange()));
        } else {
            oldDailyGroup.setInnerIn(EventUtil.plusInt(oldDailyGroup.getInnerIn(), changeDto.getQuantityChange()));
        }
        if (changeDto.getIsSowTrigger()) {
            oldDailyGroup.setUnweanCount(EventUtil.plusInt(oldDailyGroup.getUnweanCount(), changeDto.getQuantityChange()));
        }
        oldDailyGroup.setEnd(EventUtil.plusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }
}
