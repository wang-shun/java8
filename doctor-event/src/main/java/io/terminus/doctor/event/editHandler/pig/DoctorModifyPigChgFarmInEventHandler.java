package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/27.
 * 转场触发转入
 */
@Deprecated
@Component
public class DoctorModifyPigChgFarmInEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Override
    public void rollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        String key = "pig" + deletePigEvent.getPigId().toString();
        expectTrue(doctorConcurrentControl.setKey(key), "event.concurrent.error", deletePigEvent.getPigCode());

        //1.删除转场触发的事件
        doctorPigEventDao.deleteByChgFarm(deletePigEvent.getPigId());

        //2.转场转入事件
        doctorPigEventDao.delete(deletePigEvent.getId());

        //3.删除猪
        doctorPigDao.delete(deletePigEvent.getPigId());

        //4.删除猪track
        DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(deletePigEvent.getPigId());
        doctorPigTrackDao.delete(pigTrack.getId());

        //5.更新记录表
        updateDailyOfDelete(deletePigEvent);

    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(deletePigEvent.getPigId());
        return notNull(lastEvent) && Objects.equals(deletePigEvent.getId(), lastEvent.getId()) ;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent deletePigEvent) {
        DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .barnType(deletePigEvent.getBarnType())
                .pigSex(deletePigEvent.getKind())
                .entryCountChange(-1)
                .build();
        if (Objects.equals(deletePigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            changeDto2.setCfCountChange(-1);
        } else {
            changeDto2.setPhCountChange(-1);
        }
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto2));
        //旧版
       // DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        //oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        if (Objects.equals(deletePigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            updateDailySowPigLiveStock(deletePigEvent.getFarmId(), getAfterDay(deletePigEvent.getEventAt()),
                    changeDto2.getEntryCountChange(), changeDto2.getPhCountChange(), changeDto2.getCfCountChange());

            //更新空怀母猪数
            updatePhSowStatusCount(deletePigEvent, -1, deletePigEvent.getPigStatusAfter());

        } else {
            updateDailyBoarPigLiveStock(deletePigEvent.getFarmId(), getAfterDay(deletePigEvent.getEventAt()), changeDto2.getEntryCountChange());
        }
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .barnType(newPigEvent.getBarnType())
                .pigSex(newPigEvent.getKind())
                .entryCountChange(1)
                .build();
        if (Objects.equals(newPigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            changeDto2.setCfCountChange(1);
        } else {
            changeDto2.setPhCountChange(1);
        }
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto2));

        //旧版
      //  DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
        //oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt())
                    , changeDto2.getEntryCountChange(), changeDto2.getPhCountChange(), changeDto2.getCfCountChange());

            updatePhSowStatusCount(newPigEvent, 1, newPigEvent.getPigStatusAfter());
        } else {
            updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()), changeDto2.getEntryCountChange());
        }
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
           if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
               oldDailyPig.setSowCfEnd(EventUtil.plusInt(oldDailyPig.getSowCfEnd(), changeDto.getEntryCountChange()));
               oldDailyPig.setSowCfInFarmIn(EventUtil.plusInt(oldDailyPig.getSowCfInFarmIn(), changeDto.getEntryCountChange()));
           } else {
               oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getEntryCountChange()));
               oldDailyPig.setSowPhChgFarmIn(EventUtil.plusInt(oldDailyPig.getSowPhChgFarmIn(), changeDto.getEntryCountChange()));
           }

        } else {
            oldDailyPig.setBoarChgFarmIn(EventUtil.plusInt(oldDailyPig.getBoarChgFarmIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getEntryCountChange()));
        }
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
            if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
                oldDailyPig.setSowCf(EventUtil.plusInt(oldDailyPig.getSowCf(), changeDto.getEntryCountChange()));
                oldDailyPig.setSowCfEnd(EventUtil.plusInt(oldDailyPig.getSowCfEnd(), changeDto.getEntryCountChange()));
                oldDailyPig.setSowCfInFarmIn(EventUtil.plusInt(oldDailyPig.getSowCfInFarmIn(), changeDto.getEntryCountChange()));
            } else {
                oldDailyPig.setSowPh(EventUtil.plusInt(oldDailyPig.getSowPh(), changeDto.getEntryCountChange()));
                oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getEntryCountChange()));
                oldDailyPig.setSowPhChgFarmIn(EventUtil.plusInt(oldDailyPig.getSowPhChgFarmIn(), changeDto.getEntryCountChange()));
            }
            oldDailyPig.setSowIn(EventUtil.plusInt(oldDailyPig.getSowIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setSowEnd(EventUtil.plusInt(oldDailyPig.getSowEnd(), changeDto.getEntryCountChange()));

        } else {
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getEntryCountChange()));
        }
        return oldDailyPig;
    }
}
