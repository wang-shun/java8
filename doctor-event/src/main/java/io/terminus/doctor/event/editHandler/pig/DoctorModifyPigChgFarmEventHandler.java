package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/19.
 * 转场
 */
@Component
public class DoctorModifyPigChgFarmEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Autowired
    private DoctorModifyPigRemoveEventHandler modifyPigRemoveEventHandler;
    @Autowired
    private DoctorModifyPigChgFarmInEventHandler modifyPigChgFarmInEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        throw new InvalidException("chg.farm.not.allow.modify");
    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorPigEvent chgFarmInEvent = doctorPigEventDao.findByRelPigEventId(deletePigEvent.getId());
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(deletePigEvent.getPigId());
        DoctorBarn doctorBarn = doctorBarnDao.findById(deletePigEvent.getBarnId());
        return notNull(lastEvent)
                && Objects.equals(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue())
                && Objects.equals(deletePigEvent.getId(), lastEvent.getId())
                && modifyPigChgFarmInEventHandler.rollbackHandleCheck(chgFarmInEvent);
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        DoctorPigEvent chgFarmInEvent = doctorPigEventDao.findByRelPigEventId(deletePigEvent.getId());
        modifyPigChgFarmInEventHandler.rollbackHandle(chgFarmInEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPig buildNewPigForRollback(DoctorPigEvent deletePigEvent, DoctorPig oldPig) {
        return modifyPigRemoveEventHandler.buildNewPigForRollback(deletePigEvent, oldPig);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        return modifyPigRemoveEventHandler.buildNewTrackForRollback(deletePigEvent, oldPigTrack);
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())
                && !Objects.equals(oldPigEvent.getBoarType(), BoarEntryType.HGZ.getKey())) {
            return;
        }

        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .pigSex(oldPigEvent.getKind())
                .removeCountChange(-1)
                .barnType(oldPigEvent.getBarnType())
                .build();
        if (Objects.equals(oldPigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            changeDto1.setCfCountChange(1);
        } else {
            changeDto1.setPhCountChange(1);
        }
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));

        //旧版
        DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto1));

        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            updateDailySowPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()),
                    -changeDto1.getRemoveCountChange(), changeDto1.getPhCountChange(), changeDto1.getCfCountChange());

            updatePhSowStatusCount(oldPigEvent, 1, oldPigEvent.getPigStatusBefore());
        } else {
            updateDailyBoarPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()), -changeDto1.getRemoveCountChange());
        }
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())
                && !Objects.equals(newPigEvent.getBoarType(), BoarEntryType.HGZ.getKey())) {
            return;
        }
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());

        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pigSex(newPigEvent.getKind())
                .removeCountChange(1)
                .barnType(newPigEvent.getBarnType())
                .build();
        if (Objects.equals(newPigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            changeDto2.setCfCountChange(-1);
        } else {
            changeDto2.setPhCountChange(-1);
        }
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));

        //旧版
        DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
        oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()),
                    -changeDto2.getRemoveCountChange(), changeDto2.getPhCountChange(), changeDto2.getCfCountChange());

            updatePhSowStatusCount(newPigEvent, -1, newPigEvent.getPigStatusBefore());
        } else {
            updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()), -changeDto2.getRemoveCountChange());
        }
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        //公猪
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.BOAR.getKey())) {
            oldDailyPig.setBoarOtherOut(EventUtil.plusInt(oldDailyPig.getBoarOtherOut(), changeDto.getRemoveCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.minusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
           return oldDailyPig;
        }
        //母猪
        if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            oldDailyPig.setSowCfEnd(EventUtil.minusInt(oldDailyPig.getSowCfEnd(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowCfChgFarm(EventUtil.plusInt(oldDailyPig.getSowCfChgFarm(), changeDto.getRemoveCountChange()));
        } else {
            oldDailyPig.setSowPhEnd(EventUtil.minusInt(oldDailyPig.getSowPhEnd(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowPhChgFarm(EventUtil.plusInt(oldDailyPig.getSowPhChgFarm(), changeDto.getRemoveCountChange()));
        }
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        //公猪
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.BOAR.getKey())) {
            oldDailyPig.setBoarChgFarm(EventUtil.plusInt(oldDailyPig.getBoarChgFarm(), changeDto.getRemoveCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.minusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
            return oldDailyPig;
        }
        //母猪
        if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            oldDailyPig.setSowCf(EventUtil.minusInt(oldDailyPig.getSowCf(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowCfEnd(EventUtil.minusInt(oldDailyPig.getSowCf(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowCfChgFarm(EventUtil.plusInt(oldDailyPig.getSowCfChgFarm(), changeDto.getRemoveCountChange()));
        } else {
            oldDailyPig.setSowPh(EventUtil.minusInt(oldDailyPig.getSowPh(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowPhEnd(EventUtil.minusInt(oldDailyPig.getSowPh(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowPhChgFarm(EventUtil.plusInt(oldDailyPig.getSowPhChgFarm(), changeDto.getRemoveCountChange()));
        }
        oldDailyPig.setSowChgFarm(EventUtil.plusInt(oldDailyPig.getSowChgFarm(), changeDto.getRemoveCountChange()));
        oldDailyPig.setSowEnd(EventUtil.minusInt(oldDailyPig.getSowEnd(), changeDto.getRemoveCountChange()));
        return oldDailyPig;
    }


}
