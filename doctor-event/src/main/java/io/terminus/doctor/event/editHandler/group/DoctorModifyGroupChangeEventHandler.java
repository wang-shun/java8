package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 14:01 2017/4/15
 */
@Component
public class DoctorModifyGroupChangeEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        return DoctorEventChangeDto.builder()
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .oldChangeTypeId(oldInput.getChangeTypeId())
                .newChangeTypeId(newInput.getChangeTypeId())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .weightChange(EventUtil.minusDouble(newInput.getWeight(), oldInput.getWeight()))
                .priceChange(EventUtil.minusLong(newInput.getPrice(), oldInput.getPrice()))
                .overPriceChange(EventUtil.minusLong(newInput.getOverPrice(), oldInput.getOverPrice()))
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                .build();
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        newGroupEvent.setChangeTypeId(newInput.getChangeTypeId());
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setWeight(newInput.getWeight());
        newGroupEvent.setPrice(newInput.getPrice());
        newGroupEvent.setOverPrice(newInput.getOverPrice());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setQuantity(EventUtil.minusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        if (changeDto.getIsSowTrigger()) {
            oldGroupTrack.setUnweanQty(EventUtil.minusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyGroup oldDailyGroup = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            if (Objects.equals(changeDto.getNewChangeTypeId(), changeDto.getOldChangeTypeId())) {
                changeDto.setChangeTypeId(changeDto.getOldChangeTypeId());
                buildDailyGroup(oldDailyGroup, changeDto);
            } else {
                if (notNull(oldGroupEvent.getSowId())) {
                    oldDailyGroup.setUnweanCount(EventUtil.minusInt(oldDailyGroup.getUnweanCount(), changeDto.getQuantityChange()));
                }
                oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
                updateChange(oldDailyGroup, newInput.getQuantity(), changeDto.getNewChangeTypeId());
                updateChange(oldDailyGroup, EventUtil.minusInt(0 , oldInput.getQuantity()), changeDto.getNewChangeTypeId());
            }
            doctorDailyGroupDao.update(oldDailyGroup);
            updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getNewEventAt()).plusDays(1).toDate(), changeDto.getQuantityChange());
        } else {
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                    .changeTypeId(changeDto.getOldChangeTypeId())
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .quantityChange(newInput.getQuantity())
                    .changeTypeId(changeDto.getNewChangeTypeId())
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            updateDailyWhenEventDiff(changeDto, changeDto1, changeDto2);
        }
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), deleteGroupEvent.getQuantity()));
        if (notNull(deleteGroupEvent.getSowId())) {
            oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), deleteGroupEvent.getQuantity()));
        }
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, deleteGroupEvent.getQuantity()))
                .changeTypeId(deleteGroupEvent.getChangeTypeId())
                .isSowTrigger(notNull(deleteGroupEvent.getSowId()))
                .build();

        DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(deleteGroupEvent.getGroupId(), deleteGroupEvent.getEventAt());
        doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(deleteGroupEvent.getGroupId(), getAfterDay(deleteGroupEvent.getEventAt()), -changeDto1.getQuantityChange());

    }

    /**
     * 事件时间发生变化时更新日记录
     * @param changeDto
     * @param changeDto1
     * @param changeDto2
     */
    private void updateDailyWhenEventDiff(DoctorEventChangeDto changeDto, DoctorEventChangeDto changeDto1, DoctorEventChangeDto changeDto2) {
            DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getOldEventAt()), -changeDto1.getQuantityChange());

            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2));
            updateDailyGroupLiveStock(changeDto.getBusinessId(), getAfterDay(changeDto.getNewEventAt()), -changeDto2.getQuantityChange());
    }

    private DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto) {
        updateChange(oldDailyGroup, changeDto.getQuantityChange(), changeDto.getChangeTypeId());
        if (changeDto.getIsSowTrigger()) {
            oldDailyGroup.setUnweanCount(EventUtil.minusInt(oldDailyGroup.getUnweanCount(), changeDto.getQuantityChange()));
        }
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }

    /**
     * 更新变动
     * @param oldDailyGroup 原猪群日记录
     * @param quantityChange 变动数量
     * @param changeTypeId 变动类型
     */
    private void updateChange(DoctorDailyGroup oldDailyGroup, Integer quantityChange, Long changeTypeId) {
        if (Objects.equals(changeTypeId, 109L)) {
            oldDailyGroup.setSale(EventUtil.plusInt(oldDailyGroup.getSale(), quantityChange));
        } else if (Objects.equals(changeTypeId, 110L)) {
            oldDailyGroup.setDead(EventUtil.plusInt(oldDailyGroup.getDead(), quantityChange));
        } else if (Objects.equals(changeTypeId, 111L)) {
            oldDailyGroup.setWeedOut(EventUtil.plusInt(oldDailyGroup.getWeedOut(), quantityChange));
        } else {
            oldDailyGroup.setOtherChange(EventUtil.plusInt(oldDailyGroup.getOtherChange(), quantityChange));
        }
    }
}
