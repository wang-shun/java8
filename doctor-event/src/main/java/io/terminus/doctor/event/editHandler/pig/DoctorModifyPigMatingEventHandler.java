package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.Checks;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.apache.commons.lang3.time.DateUtils;
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
        DoctorPigTrack doctorPigTrack = null;
        //不允许修改初配事件日期，初配事件还要影响其他复配事件的预产期，track中预产期，比较麻烦
//        if( !doctorPigTrack.getCurrentEventId().equals(oldPigEvent.getId())) {
//            if (Objects.equals(oldPigEvent.getCurrentMatingCount(), 1)) {
//                expectTrue(Objects.equals(new DateTime(oldPigEvent.getEventAt()).withTimeAtStartOfDay(),
//                        new DateTime(inputDto.eventAt()).withTimeAtStartOfDay()),
//                        "first.mate.date.is.not.modify");
//            }
//        }

        if (oldPigEvent.getCurrentMatingCount() >= 1) {
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
    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        DoctorPigEvent firstMateEvent = doctorPigEventDao.getFirstMateEvent(oldPigTrack.getPigId(), new Date());
        if (Objects.equals(firstMateEvent.getId(), changeDto.getEventId()) && oldPigTrack.getExtraMap().containsKey("judgePregDate")) {
            oldPigTrack.getExtraMap().put("judgePregDate", firstMateEvent.getJudgePregDate());
            oldPigTrack.setExtraMap(oldPigTrack.getExtraMap());
        }
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (!DateUtils.isSameDay(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
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

        //之前,事件妊娠检查aaa
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
        //旧版
        DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto1));

        //更新配种母猪数与空怀母猪数
        Integer sowPhKonghuaiChangeCount = 0;
        if (Objects.equals(oldPigEvent.getPigStatusBefore(), PigStatus.KongHuai.getKey())
                || Objects.equals(oldPigEvent.getPigStatusBefore(), PigStatus.Entry.getKey())
                || Objects.equals(oldPigEvent.getPigStatusBefore(), PigStatus.Wean.getKey())) {

                sowPhKonghuaiChangeCount = 1;
        }
        updateDailyPhStatusLiveStock(oldPigEvent.getFarmId(), oldPigEvent.getEventAt()
                , -1, sowPhKonghuaiChangeCount , 0);

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
        //旧版
        DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
        oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        //更新配种母猪数与空怀母猪数
        Integer sowPhKonghuaiChangeCount = 0;
        if (Objects.equals(newPigEvent.getPigStatusBefore(), PigStatus.KongHuai.getKey())
                || Objects.equals(newPigEvent.getPigStatusBefore(), PigStatus.Entry.getKey())
                || Objects.equals(newPigEvent.getPigStatusBefore(), PigStatus.Wean.getKey())) {
            sowPhKonghuaiChangeCount = -1;
        }
        updateDailyPhStatusLiveStock(newPigEvent.getFarmId(), inputDto.eventAt()
                , 1, sowPhKonghuaiChangeCount, 0);
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        oldDailyPig.setMatingCount(EventUtil.plusInt(oldDailyPig.getMatingCount(), changeDto.getDoctorMateTypeCountChange()));
        DoctorMatingType matingType = DoctorMatingType.from(changeDto.getDoctorMateType());
        Checks.expectNotNull(matingType, "mating.type.is.null");
        switch (matingType) {
            case HP:
                oldDailyPig.setMateHb(EventUtil.plusInt(oldDailyPig.getMateHb(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case LPC:
                oldDailyPig.setMateLc(EventUtil.plusInt(oldDailyPig.getMateLc(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case DP:
                oldDailyPig.setMateDn(EventUtil.plusInt(oldDailyPig.getMateDn(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case YP:
                oldDailyPig.setMateYx(EventUtil.plusInt(oldDailyPig.getMateYx(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case FP:
                oldDailyPig.setMateFq(EventUtil.plusInt(oldDailyPig.getMateFq(), changeDto.getDoctorMateTypeCountChange()));
                break;
            default:
                throw new InvalidException("mating.type.error");
        }
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        DoctorMatingType matingType = DoctorMatingType.from(changeDto.getDoctorMateType());
        Checks.expectNotNull(matingType, "mating.type.is.null");
        switch (matingType) {
            case HP:
                oldDailyPig.setMateHb(EventUtil.plusInt(oldDailyPig.getMateHb(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case LPC:
                oldDailyPig.setMateLc(EventUtil.plusInt(oldDailyPig.getMateLc(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case DP:
                oldDailyPig.setMateDn(EventUtil.plusInt(oldDailyPig.getMateDn(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case YP:
                oldDailyPig.setMateYx(EventUtil.plusInt(oldDailyPig.getMateYx(), changeDto.getDoctorMateTypeCountChange()));
                break;
            case FP:
                oldDailyPig.setMateFq(EventUtil.plusInt(oldDailyPig.getMateFq(), changeDto.getDoctorMateTypeCountChange()));
                break;
            default:
                throw new InvalidException("mating.type.error");
        }
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
