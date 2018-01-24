package io.terminus.doctor.event.editHandler.pig;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/4/19.
 * 妊娠检查
 */
@Component
public class DoctorModifyPigPregCheckEventHandler extends DoctorAbstractModifyPigEventHandler {

    public static final Map<Integer, Integer> PREG_CHECK_RESULT = Maps.newHashMap();
    {
        PREG_CHECK_RESULT.put(PregCheckResult.YANG.getKey(), PregCheckResult.YANG.getKey());
        PREG_CHECK_RESULT.put(PregCheckResult.YING.getKey(), KongHuaiPregCheckResult.YING.getKey());
        PREG_CHECK_RESULT.put(PregCheckResult.FANQING.getKey(), KongHuaiPregCheckResult.FANQING.getKey());
        PREG_CHECK_RESULT.put(PregCheckResult.LIUCHAN.getKey(), KongHuaiPregCheckResult.LIUCHAN.getKey());
    }

    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        super.modifyHandleCheck(oldPigEvent, inputDto);
        DoctorPregChkResultDto pregChkResultDto = (DoctorPregChkResultDto) inputDto;
        if (!Objects.equals(oldPigEvent.getPregCheckResult(), pregChkResultDto.getCheckResult())) {
            DoctorPigEvent lastStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(oldPigEvent.getPigId(), new Date());
            if (!Objects.equals(lastStatusEvent.getId(), oldPigEvent.getId())) {
                throw new InvalidException("not.last.pregCheck.event");
            }
        }
    }

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
        newEvent.setPigStatusAfter(getStatus(newDto.getCheckResult()));
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        oldPigTrack.setStatus(getStatus(changeDto.getNewPregCheckResult()));
        Map<String, Object> extra = MoreObjects.firstNonNull(oldPigTrack.getExtraMap(), Maps.newHashMap());
        extra.put("pregCheckResult", PREG_CHECK_RESULT.get(changeDto.getNewPregCheckResult()));
        oldPigTrack.setExtraMap(extra);
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (DateUtils.isSameDay(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(changeDto.getFarmId(), changeDto.getOldEventAt());

            //1.原妊娠检查结果
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .pregCheckResult(changeDto.getOldPregCheckResult())
                    .pregCheckResultCountChange(-1)
                    .build();
            buildDailyPig(oldDailyPig, changeDto1);

            //2.新妊娠检查结果
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .pregCheckResult(changeDto.getNewPregCheckResult())
                    .pregCheckResultCountChange(1)
                    .build();
            doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto2));

            //更新配种、空怀、怀孕母猪数量
            if (!PigType.MATING_TYPES.contains(oldPigEvent.getBarnType())) {
                return;
            }

            if (Objects.equals(changeDto.getOldPregCheckResult(), PregCheckResult.YANG.getKey())
                    && !Objects.equals(changeDto.getNewPregCheckResult(), PregCheckResult.YANG.getKey())) {
                updateDailyPhStatusLiveStock(oldPigEvent.getFarmId(), oldPigEvent.getEventAt()
                        , 0, 1, -1);
            } else if (!Objects.equals(changeDto.getOldPregCheckResult(), PregCheckResult.YANG.getKey())
                    && Objects.equals(changeDto.getNewPregCheckResult(), PregCheckResult.YANG.getKey())) {
                updateDailyPhStatusLiveStock(oldPigEvent.getFarmId(), oldPigEvent.getEventAt()
                        , 0, -1, 1);
            }

        } else {
            updateDailyOfDelete(oldPigEvent);
            updateDailyOfNew(oldPigEvent, inputDto);
        }
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(deletePigEvent.getPigId(), deletePigEvent.getEventAt());
        Integer beforeStatus = getStatus(beforeStatusEvent);
        oldPigTrack.setStatus(beforeStatus);
        if (Objects.equals(beforeStatus, PigStatus.Mate.getKey())) {
            oldPigTrack.setCurrentMatingCount(beforeStatusEvent.getCurrentMatingCount());
        }
        Map<String, Object> extra = MoreObjects.firstNonNull(oldPigTrack.getExtraMap(), Maps.newHashMap());
        extra.put("pregCheckResult", beforeStatusEvent.getPregCheckResult());
        return oldPigTrack;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {

        //删除妊娠检查阳性事件,还原本胎次下最近的初配事件IsImpregnation字段
        if (Objects.equals(deletePigEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())) {
            DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(deletePigEvent.getPigId()
                    , doctorPigEventDao.findLastParity(deletePigEvent.getPigId()));

            expectTrue(notNull(firstMate), "first.mate.not.null", deletePigEvent.getPigId());
            firstMate.setIsImpregnation(IsOrNot.NO.getValue());
            doctorPigEventDao.update(firstMate);
        }
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .pregCheckResult(oldPigEvent.getPregCheckResult())
                .pregCheckResultCountChange(-1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));


        //更新配种、空怀、怀孕母猪数量
        if (!PigType.MATING_TYPES.contains(oldPigEvent.getBarnType())) {
            return;
        }
        Integer phMatingChangeCount = 0;
        Integer phKongHuaiChangeCount = 0;
        Integer phPregnantChangeCount = 0;
        Integer afterStatus = getStatus(oldPigEvent.getPregCheckResult());
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(oldPigEvent.getPigId(), oldPigEvent.getEventAt());
        Integer beforeStatus = getStatus(beforeStatusEvent);
        if (Objects.equals(beforeStatus, afterStatus)) {
            return;
        }
        if (Objects.equals(beforeStatus, PigStatus.Mate.getKey())) {
            phMatingChangeCount = 1;
        } else if (Objects.equals(beforeStatus, PigStatus.KongHuai.getKey())) {
            phKongHuaiChangeCount = 1;
        } else {
            phPregnantChangeCount = 1;
        }

        if (Objects.equals(afterStatus, PigStatus.KongHuai.getKey())) {
            phKongHuaiChangeCount = -1;
        } else {
            phPregnantChangeCount = -1;
        }
        updateDailyPhStatusLiveStock(oldPigEvent.getFarmId(), oldPigEvent.getEventAt()
                , phMatingChangeCount, phKongHuaiChangeCount, phPregnantChangeCount);
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorPregChkResultDto newDto = (DoctorPregChkResultDto) inputDto;
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), newDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pregCheckResult(newDto.getCheckResult())
                .pregCheckResultCountChange(1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));


        //更新配种、空怀、怀孕母猪数量
        if (!PigType.MATING_TYPES.contains(newPigEvent.getBarnType())) {
            return;
        }
        Integer afterStatus = getStatus(newDto.getCheckResult());
        Integer beforeStatus = newPigEvent.getPigStatusBefore();
        if (Objects.equals(beforeStatus, afterStatus)) {
            return;
        }

        Integer phMatingChangeCount = 0;
        Integer phKongHuaiChangeCount= 0;
        Integer phPregnantChangeCount = 0;
        if (Objects.equals(beforeStatus, PigStatus.Mate.getKey())) {
            phMatingChangeCount = -1;
        }
        else if (Objects.equals(newPigEvent.getPigStatusBefore(), PigStatus.KongHuai.getKey())) {
            phKongHuaiChangeCount = -1;
        } else {
            phPregnantChangeCount = -1;
        }

        if (Objects.equals(afterStatus, PigStatus.KongHuai.getKey())) {
            phKongHuaiChangeCount = 1;
        } else {
            phPregnantChangeCount = 1;
        }
        updateDailyPhStatusLiveStock(newPigEvent.getFarmId(), inputDto.eventAt()
                , phMatingChangeCount, phKongHuaiChangeCount, phPregnantChangeCount);

    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        PregCheckResult checkResult = PregCheckResult.from(changeDto.getPregCheckResult());
        expectTrue(notNull(checkResult), "preg.check.result.error", changeDto.getPregCheckResult());
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
                throw new InvalidException("preg.check.result.error", checkResult.getKey());
        }
        return oldDailyPig;
    }

    private Integer getStatus(Integer pregCheckResult) {
        return Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())
                ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey();
    }

    private Integer getStatus(DoctorPigEvent beforeStatusEvent) {
        if (Objects.equals(beforeStatusEvent.getType(), PigEvent.MATING.getKey())) {
            return PigStatus.Mate.getKey();
        }

        return Objects.equals(beforeStatusEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())
                ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey();
    }

}

