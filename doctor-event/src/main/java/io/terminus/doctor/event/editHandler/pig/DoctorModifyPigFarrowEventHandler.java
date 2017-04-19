package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.model.DoctorDailyPig;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * Created by xjn on 17/4/14.
 * 分娩编辑和回滚
 */
@Component
public class DoctorModifyPigFarrowEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Autowired
    private DoctorModifyGroupMoveInEventHandler doctorModifyMoveInEventHandler;
    @Autowired
    private DoctorSowFarrowingHandler doctorSowFarrowingHandler;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) inputDto;
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .farrowWeightChange(EventUtil.minusDouble(newDto.getBirthNestAvg(), oldDto.getBirthNestAvg()))
                .liveCountChange(EventUtil.minusInt(newDto.getFarrowingLiveCount(), oldDto.getFarrowingLiveCount()))
                .healthCountChange(EventUtil.minusInt(newDto.getHealthCount(), oldDto.getHealthCount()))
                .weakCountChange(EventUtil.minusInt(newDto.getWeakCount(), oldDto.getWeakCount()))
                .mnyCountChange(EventUtil.minusInt(newDto.getMnyCount(), oldDto.getMnyCount()))
                .blackCountChange(EventUtil.minusInt(newDto.getBlackCount(), oldDto.getBlackCount()))
                .deadCountChange(EventUtil.minusInt(newDto.getDeadCount(), oldDto.getDeadCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) inputDto;
        newEvent.setFarrowWeight(newDto.getBirthNestAvg());
        newEvent.setLiveCount(newDto.getFarrowingLiveCount());
        newEvent.setHealthCount(newDto.getHealthCount());
        newEvent.setWeakCount(newDto.getWeakCount());
        newEvent.setMnyCount(newDto.getMnyCount());
        newEvent.setBlackCount(newDto.getBlackCount());
        newEvent.setDeadCount(newDto.getDeadCount());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            return oldPigTrack;
        }
        oldPigTrack.setFarrowQty(EventUtil.plusInt(oldPigTrack.getFarrowQty(), changeDto.getLiveCountChange()));
        oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), changeDto.getLiveCountChange()));
        oldPigTrack.setFarrowAvgWeight(EventUtil.plusDouble(oldPigTrack.getFarrowAvgWeight(),
                EventUtil.getAvgWeight(changeDto.getFarrowWeightChange(), changeDto.getLiveCountChange())));
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyPig oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        } else {
            //更新新时间的日记录
            DoctorDailyPig oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            DoctorFarrowingDto farrowingDto1 = (DoctorFarrowingDto) inputDto;
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .liveCountChange(farrowingDto1.getFarrowingLiveCount())
                    .healthCountChange(farrowingDto1.getHealthCount())
                    .weakCountChange(farrowingDto1.getWeakCount())
                    .blackCountChange(farrowingDto1.getBlackCount())
                    .deadCountChange(farrowingDto1.getDeadCount())
                    .jxCountChange(farrowingDto1.getJxCount())
                    .mnyCountChange(farrowingDto1.getMnyCount())
                    .farrowWeightChange(farrowingDto1.getBirthNestAvg())
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));

            //更新原时间的日记录
            DoctorDailyPig oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorFarrowingDto farrowingDto2 = JSON_MAPPER.fromJson(oldEvent.getExtra(), DoctorFarrowingDto.class);
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .liveCountChange(EventUtil.minusInt(0, farrowingDto2.getFarrowingLiveCount()))
                    .healthCountChange(EventUtil.minusInt(0, farrowingDto2.getHealthCount()))
                    .weakCountChange(EventUtil.minusInt(0, farrowingDto2.getWeakCount()))
                    .blackCountChange(EventUtil.minusInt(0, farrowingDto2.getBlackCount()))
                    .deadCountChange(EventUtil.minusInt(0, farrowingDto2.getDeadCount()))
                    .jxCountChange(EventUtil.minusInt(0, farrowingDto2.getJxCount()))
                    .mnyCountChange(EventUtil.minusInt(0, farrowingDto2.getMnyCount()))
                    .farrowWeightChange(EventUtil.minusDouble(0D, farrowingDto2.getBirthNestAvg()))
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        // TODO: 17/4/19 新建编辑
        //转入编辑
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyMoveInEventHandler.modifyHandle(oldGroupEvent, doctorSowFarrowingHandler.buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected DoctorDailyPig buildDailyPig(DoctorDailyPig oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig.setFarrowLive(EventUtil.plusInt(oldDailyPig.getFarrowLive(), changeDto.getLiveCountChange()));
        oldDailyPig.setFarrowHealth(EventUtil.plusInt(oldDailyPig.getFarrowHealth(), changeDto.getHealthCountChange()));
        oldDailyPig.setFarrowWeak(EventUtil.plusInt(oldDailyPig.getFarrowWeak(), changeDto.getWeakCountChange()));
        oldDailyPig.setFarrowBlack(EventUtil.plusInt(oldDailyPig.getFarrowBlack(), changeDto.getBlackCountChange()));
        oldDailyPig.setFarrowDead(EventUtil.plusInt(oldDailyPig.getFarrowDead(), changeDto.getDeadCountChange()));
        oldDailyPig.setFarrowJx(EventUtil.plusInt(oldDailyPig.getFarrowJx(), changeDto.getJxCountChange()));
        oldDailyPig.setFarrowMny(EventUtil.plusInt(oldDailyPig.getFarrowMny(), changeDto.getMnyCountChange()));
        oldDailyPig.setFarrowWeight(EventUtil.plusDouble(oldDailyPig.getFarrowWeight(), changeDto.getFarrowWeightChange()));
        oldDailyPig.setFarrowAvgWeight(EventUtil.getAvgWeight(oldDailyPig.getFarrowWeight(), oldDailyPig.getFarrowLive()));
        return oldDailyPig;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        // TODO: 17/4/19 新建回滚
        //转入回滚
        DoctorGroupEvent deleteGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyMoveInEventHandler.rollbackHandle(deleteGroupEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(PigStatus.Farrow.getKey());
        oldPigTrack.setFarrowAvgWeight(0D);
        oldPigTrack.setFarrowQty(0);
        oldPigTrack.setUnweanQty(0);
        oldPigTrack.setGroupId(-1L);
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        DoctorDailyPig oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        oldDailyPig.setFarrowNest(EventUtil.minusInt(oldDailyPig.getFarrowNest(), 1));
        oldDailyPig.setFarrowLive(EventUtil.minusInt(oldDailyPig.getFarrowLive(), deletePigEvent.getLiveCount()));
        oldDailyPig.setFarrowHealth(EventUtil.minusInt(oldDailyPig.getFarrowHealth(), deletePigEvent.getHealthCount()));
        oldDailyPig.setFarrowWeak(EventUtil.minusInt(oldDailyPig.getFarrowWeak(), deletePigEvent.getWeakCount()));
        oldDailyPig.setFarrowBlack(EventUtil.minusInt(oldDailyPig.getFarrowBlack(), deletePigEvent.getBlackCount()));
        oldDailyPig.setFarrowDead(EventUtil.minusInt(oldDailyPig.getFarrowDead(), deletePigEvent.getDeadCount()));
        oldDailyPig.setFarrowJx(EventUtil.minusInt(oldDailyPig.getFarrowJx(), deletePigEvent.getJxCount()));
        oldDailyPig.setFarrowMny(EventUtil.minusInt(oldDailyPig.getFarrowMny(), deletePigEvent.getMnyCount()));
        oldDailyPig.setFarrowWeight(EventUtil.minusDouble(oldDailyPig.getFarrowWeight(), deletePigEvent.getFarrowWeight()));
        oldDailyPig.setFarrowAvgWeight(EventUtil.getAvgWeight(oldDailyPig.getFarrowWeight(), oldDailyPig.getFarrowLive()));
        doctorDailyPigDao.update(oldDailyPig);
    }
}
