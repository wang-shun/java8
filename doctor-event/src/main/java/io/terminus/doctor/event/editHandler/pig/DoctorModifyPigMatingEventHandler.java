package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.editHandler.pig.DoctorModifyPigPregCheckEventHandler.PREG_CHECK_RESULT;

/**
 * Created by terminus on 2017/4/17.
 * 配种
 */
@Component
public class DoctorModifyPigMatingEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        super.modifyHandleCheck(oldPigEvent, inputDto);

        //不允许修改初配事件日期，初配事件还要影响其他复配事件的预产期，track中预产期，比较麻烦
        if (Objects.equals(oldPigEvent.getCurrentMatingCount(), 1)) {
            expectTrue(Objects.equals(new DateTime(oldPigEvent.getEventAt()).withTimeAtStartOfDay(),
                    new DateTime(inputDto.eventAt()).withTimeAtStartOfDay()),
                    "first.mate.date.is.not.modify");
        }

        if (oldPigEvent.getCurrentMatingCount() > 1) {
            serialMateValid(oldPigEvent.getPigId(), oldPigEvent.getParity(), inputDto.eventAt());
        }
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldPigEvent.getEventAt())
                .newEventAt(inputDto.eventAt())
                .eventId(oldPigEvent.getId())
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorMatingDto newDto = (DoctorMatingDto) inputDto;
        doctorPigEvent.setMateType(newDto.getMatingType());
        doctorPigEvent.setBoarCode(newDto.getMatingBoarPigCode());
        doctorPigEvent.setOperatorName(newDto.getOperatorName());
        doctorPigEvent.setOperatorId(newDto.getOperatorId());
        return doctorPigEvent;
    }

    //因为不允许修改初配日期，不会影响track，所以注释，如果以后可以编辑初配事件再打开
//    @Override
//    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoorEventChangeDto changeDto) {
//        DoctorPigEvent firstMateEvent = doctorPigEventDao.getFirstMateEvent(oldPigTrack.getPigId(), new Date());
//        if (Objects.equals(firstMateEvent.getId(), changeDto.getEventId()) && oldPigTrack.getExtraMap().containsKey("judgePregDate")) {
//            oldPigTrack.getExtraMap().put("judgePregDate", firstMateEvent.getJudgePregDate());
//            oldPigTrack.setExtraMap(oldPigTrack.getExtraMap());
//        }
//        return oldPigTrack;
//    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            //原日期记录更新
            updateDailyOfDelete(oldPigEvent);

            //新日记记录更新
            updateDailyOfNew(oldPigEvent, inputDto);
        }
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(deletePigEvent.getPigId(), deletePigEvent.getEventAt());
        oldPigTrack.setCurrentMatingCount(EventUtil.minusInt(oldPigTrack.getCurrentMatingCount(), 1));
        Map<String, Object> extra = Maps.newHashMap();

        //之前,事件进场
        if (Objects.equals(beforeStatusEvent.getType(), PigEvent.ENTRY.getKey())) {
            oldPigTrack.setStatus(PigStatus.Entry.getKey());
            extra.put("enterToMate", true);
            oldPigTrack.setExtraMap(extra);
            return oldPigTrack;
        }

        //之前,事件配种
        if (Objects.equals(beforeStatusEvent.getType(), PigEvent.MATING.getKey())) {
            oldPigTrack.setStatus(PigStatus.Mate.getKey());
            return oldPigTrack;
        }

        //之前,事件妊娠检查
        if (Objects.equals(beforeStatusEvent.getType(), PigEvent.PREG_CHECK.getKey())) {
            DoctorPigEvent firstMateEvent = doctorPigEventDao.getFirstMateEvent(deletePigEvent.getPigId(), deletePigEvent.getEventAt());
            oldPigTrack.setStatus(PigStatus.KongHuai.getKey());
            extra.put("pregCheckResult", PREG_CHECK_RESULT.get(beforeStatusEvent.getPregCheckResult()));
            extra.put("judgePregDate", firstMateEvent.getJudgePregDate());
            oldPigTrack.setExtraMap(extra);
            return oldPigTrack;
        }

        //之前,事件断奶
        oldPigTrack.setStatus(PigStatus.Wean.getKey());
        oldPigTrack.setCurrentParity(EventUtil.minusInt(oldPigTrack.getCurrentParity(), 1));
        extra.put("hasWeanToMating", true);
        oldPigTrack.setExtraMap(extra);
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        if (oldPigEvent.getCurrentMatingCount() > 1) {
            return;
        }
        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .doctorMateType(oldPigEvent.getDoctorMateType())
                .doctorMateTypeCountChange(-1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        if (newPigEvent.getCurrentMatingCount() > 1) {
            return;
        }
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .doctorMateType(newPigEvent.getDoctorMateType())
                .doctorMateTypeCountChange(1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        oldDailyPig.setMatingCount(EventUtil.plusInt(oldDailyPig.getMatingCount(), changeDto.getDoctorMateTypeCountChange()));
        return oldDailyPig;
    }

    public void serialMateValid(Long pigId, Integer parity, Date eventAt) {
        DoctorPigEvent firstMatingEvent = doctorPigEventDao.queryLastFirstMate(pigId
                , parity);
        expectNotNull(firstMatingEvent, "first.mate.not.null", pigId);
        expectTrue(DateUtil.getDeltaDays(firstMatingEvent.getEventAt(), eventAt) <= 3,
                "serial.mating.over.three.day", DateUtil.toDateString(firstMatingEvent.getEventAt()));
    }

}
