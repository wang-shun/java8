package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupWeanEventHandler;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.sow.DoctorSowWeanHandler;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * Created by xjn on 17/4/17.
 * 断奶编辑回滚处理
 */
@Component
public class DoctorModifyPigWeanEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Autowired
    private DoctorSowWeanHandler doctorSowWeanHandler;
    @Autowired
    private DoctorModifyGroupWeanEventHandler modifyGroupWeanEventHandler;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorWeanDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorWeanDto.class);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .weanCountChange(EventUtil.minusInt(newDto.getWeanPigletsCount(), oldDto.getPartWeanPigletsCount()))
                .weanAvgWeight(newDto.getPartWeanAvgWeight())
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        newEvent.setWeanCount(newDto.getPartWeanPigletsCount());
        newEvent.setWeanAvgWeight(newDto.getPartWeanAvgWeight());
        return newEvent;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventId(newPigEvent.getId());
        modifyGroupWeanEventHandler.modifyHandle(oldGroupEvent, doctorSowWeanHandler.buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        } else {
            DoctorPigEvent farrowEvent = doctorPigEventDao.getFarrowEventByParity(oldPigEvent.getPigId(), oldPigEvent.getParity());
            DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .weanAvgWeight(oldPigEvent.getWeanAvgWeight())
                    .weanCountChange(-oldPigEvent.getWeanCount())
                    .weanNestChange(-1)
                    .weanDayAge(DateUtil.getDeltaDays(farrowEvent.getEventAt(), changeDto.getOldEventAt()))
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));

            DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            DoctorWeanDto weanDto2 = (DoctorWeanDto) inputDto;
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .weanAvgWeight(weanDto2.getPartWeanAvgWeight())
                    .weanCountChange(weanDto2.getWeanPigletsCount())
                    .weanNestChange(1)
                    .weanDayAge(DateUtil.getDeltaDays(farrowEvent.getEventAt(), changeDto.getNewEventAt()))
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
        }

    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig.setWeanDayAge(EventUtil.getAvgDayAge(oldDailyPig.getWeanDayAge(), oldDailyPig.getWeanCount(), changeDto.getWeanDayAge().doubleValue(), changeDto.getWeanCountChange()));
        oldDailyPig.setWeanAvgWeight(EventUtil.getAvgWeight(oldDailyPig.getWeanAvgWeight(), oldDailyPig.getWeanCount(), changeDto.getWeanAvgWeight(), changeDto.getWeanCountChange()));
        oldDailyPig.setWeanNest(EventUtil.plusInt(oldDailyPig.getWeanNest(), changeDto.getWeanNestChange()));
        oldDailyPig.setWeanCount(EventUtil.plusInt(oldDailyPig.getWeanCount(), changeDto.getWeanCountChange()));
        return oldDailyPig;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent weanGroupEvent = doctorGroupEventDao.findByRelPigEventId(deletePigEvent.getId());
        modifyGroupWeanEventHandler.rollbackHandle(weanGroupEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(PigStatus.FEED.getKey());
        oldPigTrack.setUnweanQty(deletePigEvent.getWeanCount());
        DoctorPigEvent farrowEvent = doctorPigEventDao.getFarrowEventByParity(deletePigEvent.getPigId(), deletePigEvent.getParity());
        oldPigTrack.setFarrowQty(farrowEvent.getLiveCount());
        oldPigTrack.setWeanQty(EventUtil.minusInt(oldPigTrack.getFarrowQty(), oldPigTrack.getUnweanQty()));
        oldPigTrack.setFarrowAvgWeight(EventUtil.getAvgWeight(farrowEvent.getWeight(), farrowEvent.getLiveCount()));
        oldPigTrack.setWeanAvgWeight(0D);
        oldPigTrack.setExtraMap(Maps.newHashMap());
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorPigEvent farrowEvent = doctorPigEventDao.getFarrowEventByParity(deletePigEvent.getPigId(), deletePigEvent.getParity());
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder()
                .weanNestChange(-1)
                .weanCountChange(-deletePigEvent.getWeanCount())
                .weanAvgWeight(deletePigEvent.getWeanAvgWeight())
                .weanDayAge(DateUtil.getDeltaDays(farrowEvent.getEventAt(), deletePigEvent.getEventAt()))
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
    }
}
