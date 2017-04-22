package io.terminus.doctor.event.editHandler.pig;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 17/4/19.
 * 妊娠检查
 */
@Component
public class DoctorModifyPigPregCheckEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPregChkResultDto newDto = (DoctorPregChkResultDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldPigEvent.getEventAt())
                .newEventAt(newDto.eventAt())
                .oldPregCheckResult(oldPigEvent.getPregCheckResult())
                .newPregCheckResult(newDto.getCheckResult())
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorPregChkResultDto newDto = (DoctorPregChkResultDto) inputDto;
        newEvent.setPregCheckResult(newDto.getCheckResult());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        Integer newStatus = Objects.equals(changeDto.getNewPregCheckResult(), PregCheckResult.YANG.getKey())
                ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey();
        oldPigTrack.setStatus(newStatus);
        Map<String, Object> extra = MoreObjects.firstNonNull(oldPigTrack.getExtraMap(), Maps.newHashMap());
        extra.put("pregCheckResult", changeDto.getNewPregCheckResult());
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .pregCheckResult(changeDto.getOldPregCheckResult())
                    .pregCheckResultCountChange(-1)
                    .build();
            buildDailyPig(oldDailyPig, changeDto1);
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .pregCheckResult(changeDto.getNewPregCheckResult())
                    .pregCheckResultCountChange(1)
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto2));
        } else {
            updateDailyOfDelete(oldPigEvent);
            updateDailyOfNew(oldPigEvent, inputDto);
        }
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(deletePigEvent.getPigId(), deletePigEvent.getEventAt());
        if (Objects.equals(beforeStatusEvent.getType(), PigEvent.MATING.getKey())) {
            oldPigTrack.setStatus(PigStatus.Mate.getKey());
            oldPigTrack.setCurrentMatingCount(beforeStatusEvent.getCurrentMatingCount());
            return oldPigTrack;
        }

        if (Objects.equals(beforeStatusEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())) {
            oldPigTrack.setStatus(PigStatus.Pregnancy.getKey());
        } else {
            oldPigTrack.setStatus(PigStatus.KongHuai.getKey());
        }
        Map<String, Object> extra = MoreObjects.firstNonNull(oldPigTrack.getExtraMap(), Maps.newHashMap());
        extra.put("pregCheckResult", beforeStatusEvent.getPregCheckResult());
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .pregCheckResult(oldPigEvent.getPregCheckResult())
                .pregCheckResultCountChange(-1)
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorPregChkResultDto newDto = (DoctorPregChkResultDto) inputDto;
        DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), newDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pregCheckResult(newDto.getCheckResult())
                .pregCheckResultCountChange(1)
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
    }


    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        PregCheckResult checkResult = PregCheckResult.from(changeDto.getPregCheckResult());
        Checks.expectNotNull(checkResult, "");
        switch (checkResult) {
            case YANG :
                oldDailyPig.setPregPositive(EventUtil.plusInt(oldDailyPig.getPregPositive(), changeDto.getPregCheckResultCountChange()));
                break;
            case FANQING:
                oldDailyPig.setPregFanqing(EventUtil.plusInt(oldDailyPig.getPregFanqing(), changeDto.getPregCheckResultCountChange()));
                break;
            case LIUCHAN:
                oldDailyPig.setPregLiuchan(EventUtil.plusInt(oldDailyPig.getPregLiuchan(), changeDto.getPregCheckResultCountChange()));
                break;
            case YING:
                oldDailyPig.setPregNegative(EventUtil.plusInt(oldDailyPig.getPregNegative(), changeDto.getPregCheckResultCountChange()));
                break;
            default:
                throw new InvalidException("");
        }
        return oldDailyPig;
    }
}

