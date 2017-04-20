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

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 14:01 2017/4/15
 */

public class DoctorModifyGroupChangeEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        return DoctorEventChangeDto.builder()
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .oldChangeTypeId(oldInput.getChangeTypeId())
                .changeTypeId(newInput.getChangeTypeId())
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
        DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getOldEventAt());
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            if (Objects.equals(changeDto.getChangeTypeId(), changeDto.getOldChangeTypeId())) {
                buildDailyGroup(oldDailyGroup1, changeDto, changeDto.getChangeTypeId());
            } else {
                if (notNull(oldGroupEvent.getSowId())) {
                    oldDailyGroup1.setUnweanCount(EventUtil.minusInt(oldDailyGroup1.getUnweanCount(), changeDto.getQuantityChange()));
                }
                oldDailyGroup1.setEnd(EventUtil.minusInt(oldDailyGroup1.getEnd(), changeDto.getQuantityChange()));
                updateChange(oldDailyGroup1, newInput.getQuantity(), changeDto.getChangeTypeId());
                updateChange(oldDailyGroup1, EventUtil.minusInt(0 , oldInput.getQuantity()), changeDto.getChangeTypeId());
            }
            doctorDailyGroupDao.update(oldDailyGroup1);
            updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getNewEventAt()).plusDays(1).toDate(), changeDto.getQuantityChange());
        } else {
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            DoctorDailyGroup oldDailyGroup2 = doctorDailyGroupDao.findByGroupIdAndSumAt(changeDto.getBusinessId(), changeDto.getNewEventAt());
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .quantityChange(newInput.getQuantity())
                    .isSowTrigger(changeDto.getIsSowTrigger())
                    .build();

            if (changeDto.getOldEventAt().before(changeDto.getNewEventAt())) {
                doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, changeDto.getOldChangeTypeId()));
                updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getOldEventAt()).plusDays(1).toDate(), -changeDto1.getQuantityChange());

                doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, changeDto.getChangeTypeId()));
                updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getNewEventAt()).plusDays(1).toDate(), -changeDto2.getQuantityChange());
            } else {
                doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup2, changeDto2, changeDto.getChangeTypeId()));
                updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getNewEventAt()).plusDays(1).toDate(), -changeDto2.getQuantityChange());

                doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1, changeDto.getOldChangeTypeId()));
                updateDailyGroupLiveStock(changeDto.getBusinessId(), new DateTime(changeDto.getOldEventAt()).plusDays(1).toDate(), -changeDto1.getQuantityChange());
            }
        }
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        return super.buildNewTrackForRollback(deleteGroupEvent, oldGroupTrack);
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        super.updateDailyForDelete(deleteGroupEvent);
    }

    private DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto, Long changeTypeId) {
        updateChange(oldDailyGroup, changeDto.getQuantityChange(), changeTypeId);
        if (changeDto.getIsSowTrigger()) {
            oldDailyGroup.setUnweanCount(EventUtil.minusInt(oldDailyGroup.getUnweanCount(), changeDto.getQuantityChange()));
        }
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }

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
