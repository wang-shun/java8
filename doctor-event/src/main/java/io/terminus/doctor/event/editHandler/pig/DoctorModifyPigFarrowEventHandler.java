package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.model.DoctorDailyReport;
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
        DoctorEventChangeDto changeDto = buildEventChange(oldDto, newDto);
        changeDto.setFarmId(oldPigEvent.getFarmId());
        changeDto.setBusinessId(oldPigEvent.getPigId());
        return changeDto;
    }

    @Override
    protected DoctorEventChangeDto buildEventChange(BasePigEventInputDto oldInputDto, BasePigEventInputDto newInputDto) {
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) newInputDto;
        DoctorFarrowingDto oldDto = (DoctorFarrowingDto) oldInputDto;
        return DoctorEventChangeDto.builder()
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
            DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        } else {
            DoctorFarrowingDto farrowingDto1 = (DoctorFarrowingDto) inputDto;
            DoctorEventChangeDto changeDto1 = buildEventChange(new DoctorFarrowingDto(), farrowingDto1);
            DoctorFarrowingDto farrowingDto2 = JSON_MAPPER.fromJson(oldEvent.getExtra(), DoctorFarrowingDto.class);
            DoctorEventChangeDto changeDto2 = buildEventChange(farrowingDto2, new DoctorFarrowingDto());

            DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));

            //更新原时间的日记录
            DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
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
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
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
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(deletePigEvent.getExtra(), DoctorFarrowingDto.class);
        oldDailyPig = buildDailyPig(oldDailyPig, buildEventChange(oldDto, new DoctorFarrowingDto()));
        oldDailyPig.setFarrowNest(EventUtil.minusInt(oldDailyPig.getFarrowNest(), 1));
        doctorDailyPigDao.update(oldDailyPig);
    }
}
