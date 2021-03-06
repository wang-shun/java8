package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/21.
 * 离场
 */
@Component
public class DoctorModifyPigRemoveEventHandler extends DoctorAbstractModifyPigEventHandler {

    /**
     * 销售
     */
    public static final Long SALE = 109L;

    /**
     * 死亡
     */
    public static final Long DEAD = 110L;

    /**
     * 淘汰
     */
    public static final Long WEED = 111L;

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorBarn doctorBarn = doctorBarnDao.findById(deletePigEvent.getBarnId());
        return Objects.equals(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue());
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorRemovalDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorRemovalDto.class);
        DoctorRemovalDto newDto = (DoctorRemovalDto) inputDto;

        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldDto.eventAt())
                .newEventAt(newDto.eventAt())
                .oldChangeTypeId(oldDto.getChgTypeId())
                .newChangeTypeId(newDto.getChgTypeId())
                .pigSex(oldPigEvent.getKind())
                .barnType(oldPigEvent.getBarnType())
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorRemovalDto newDto = (DoctorRemovalDto) inputDto;
        newEvent.setChangeTypeId(newDto.getChgTypeId());
        newEvent.setWeight(newDto.getWeight());
        newEvent.setPrice(newDto.getPrice());
        newEvent.setAmount(EventUtil.getAmount(newEvent.getPrice(), newEvent.getWeight()));
        newEvent.setCustomerId(newDto.getCustomerId());
        newEvent.setCustomerName(newDto.getCustomerName());
        if (Objects.equals(newDto.getChgTypeId(), DoctorBasicEnums.DEAD.getId())
                || Objects.equals(newDto.getChgTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            updateNpd(newEvent);
        }
        return newEvent;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (DateUtils.isSameDay(changeDto.getNewEventAt(), changeDto.getOldEventAt())
                && !Objects.equals(changeDto.getNewChangeTypeId(), changeDto.getOldChangeTypeId())) {
            DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .pigSex(changeDto.getPigSex())
                    .changeTypeId(changeDto.getOldChangeTypeId())
                    .removeCountChange(-1)
                    .barnType(changeDto.getBarnType())
                    .build();
            buildDailyPig(oldDailyPig, changeDto1);
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .pigSex(changeDto.getPigSex())
                    .changeTypeId(changeDto.getNewChangeTypeId())
                    .removeCountChange(1)
                    .barnType(changeDto.getBarnType())
                    .build();
            doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto2));

            //旧版
         ///  DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
         //   oldBuildDailyPig(oldDailyReport, changeDto1);
         //   oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

            return;
        }
        updateDailyOfDelete(oldPigEvent);
        updateDailyOfNew(oldPigEvent, inputDto);

    }

    @Override
    protected DoctorPig buildNewPigForRollback(DoctorPigEvent deletePigEvent, DoctorPig oldPig) {
        oldPig.setIsRemoval(IsOrNot.NO.getValue());
        return oldPig;
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(doctorEventBaseHelper.getStatusBeforeEventAt(deletePigEvent.getPigId(), deletePigEvent.getEventAt()));
        oldPigTrack.setIsRemoval(IsOrNot.NO.getValue());
        return oldPigTrack;
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
                .changeTypeId(oldPigEvent.getChangeTypeId())
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
      //  DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
       // oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto1));

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
        DoctorRemovalDto newDto = (DoctorRemovalDto) inputDto;
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), newDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pigSex(newPigEvent.getKind())
                .changeTypeId(newDto.getChgTypeId())
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
      //  DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
       // oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()),
                    -changeDto2.getRemoveCountChange(), changeDto2.getPhCountChange(), changeDto2.getCfCountChange());

            updatePhSowStatusCount(newPigEvent, -1, newPigEvent.getPigStatusBefore());
        } else {
            updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()), -changeDto2.getRemoveCountChange());
        }
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {

            if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {

                //(1).产房
                if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                    oldDailyPig.setSowCfSale(EventUtil.plusInt(oldDailyPig.getSowCfSale(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)){
                    oldDailyPig.setSowCfDead(EventUtil.plusInt(oldDailyPig.getSowCfDead(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                    oldDailyPig.setSowCfWeedOut(EventUtil.plusInt(oldDailyPig.getSowCfWeedOut(), changeDto.getRemoveCountChange()));
                } else {
                    oldDailyPig.setSowCfOtherOut(EventUtil.plusInt(oldDailyPig.getSowCfOtherOut(), changeDto.getRemoveCountChange()));
                }
                oldDailyPig.setSowCfEnd(EventUtil.minusInt(oldDailyPig.getSowCfEnd(), changeDto.getRemoveCountChange()));
            } else {

                //(2).配怀
                if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                    oldDailyPig.setSowPhSale(EventUtil.plusInt(oldDailyPig.getSowPhSale(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)) {
                    oldDailyPig.setSowPhDead(EventUtil.plusInt(oldDailyPig.getSowPhDead(), changeDto.getRemoveCountChange()));

                } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                    oldDailyPig.setSowPhWeedOut(EventUtil.plusInt(oldDailyPig.getSowPhWeedOut(), changeDto.getRemoveCountChange()));
                } else {
                    oldDailyPig.setSowPhOtherOut(EventUtil.plusInt(oldDailyPig.getSowPhOtherOut(), changeDto.getRemoveCountChange()));
                }
                oldDailyPig.setSowPhEnd(EventUtil.minusInt(oldDailyPig.getSowPhEnd(), changeDto.getRemoveCountChange()));
            }
        } else {

            //2.公猪
            if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                oldDailyPig.setBoarSale(EventUtil.plusInt(oldDailyPig.getBoarSale(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)){
                oldDailyPig.setBoarDead(EventUtil.plusInt(oldDailyPig.getBoarDead(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                oldDailyPig.setBoarWeedOut(EventUtil.plusInt(oldDailyPig.getBoarWeedOut(), changeDto.getRemoveCountChange()));
            } else {
                oldDailyPig.setBoarOtherOut(EventUtil.plusInt(oldDailyPig.getBoarOtherOut(), changeDto.getRemoveCountChange()));
            }
            oldDailyPig.setBoarEnd(EventUtil.minusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
        }
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {

            //1.母猪
            if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                oldDailyPig.setSowSale(EventUtil.plusInt(oldDailyPig.getSowSale(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)){
                oldDailyPig.setSowDead(EventUtil.plusInt(oldDailyPig.getSowDead(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                oldDailyPig.setSowWeedOut(EventUtil.plusInt(oldDailyPig.getSowWeedOut(), changeDto.getRemoveCountChange()));
            } else {
                oldDailyPig.setSowOtherOut(EventUtil.plusInt(oldDailyPig.getSowOtherOut(), changeDto.getRemoveCountChange()));
            }

            if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {

                //(1).产房
                if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                    oldDailyPig.setSowCfSale(EventUtil.plusInt(oldDailyPig.getSowCfSale(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)){
                    oldDailyPig.setSowCfDead(EventUtil.plusInt(oldDailyPig.getSowCfDead(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                    oldDailyPig.setSowCfWeedOut(EventUtil.plusInt(oldDailyPig.getSowCfWeedOut(), changeDto.getRemoveCountChange()));
                } else {
                    oldDailyPig.setSowCfOtherOut(EventUtil.plusInt(oldDailyPig.getSowCfOtherOut(), changeDto.getRemoveCountChange()));
                }
                oldDailyPig.setSowCf(EventUtil.minusInt(oldDailyPig.getSowCf(), changeDto.getRemoveCountChange()));
                oldDailyPig.setSowCfEnd(EventUtil.minusInt(oldDailyPig.getSowCfEnd(), changeDto.getRemoveCountChange()));
            } else {

                //(2).配怀
                if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                    oldDailyPig.setSowPhSale(EventUtil.plusInt(oldDailyPig.getSowPhSale(), changeDto.getRemoveCountChange()));
                } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)) {
                    oldDailyPig.setSowPhDead(EventUtil.plusInt(oldDailyPig.getSowPhDead(), changeDto.getRemoveCountChange()));

                } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                    oldDailyPig.setSowPhWeedOut(EventUtil.plusInt(oldDailyPig.getSowPhWeedOut(), changeDto.getRemoveCountChange()));
                } else {
                    oldDailyPig.setSowPhOtherOut(EventUtil.plusInt(oldDailyPig.getSowPhOtherOut(), changeDto.getRemoveCountChange()));
                }
                oldDailyPig.setSowPh(EventUtil.minusInt(oldDailyPig.getSowPh(), changeDto.getRemoveCountChange()));
                oldDailyPig.setSowPhEnd(EventUtil.minusInt(oldDailyPig.getSowPhEnd(), changeDto.getRemoveCountChange()));
            }
            oldDailyPig.setSowEnd(EventUtil.minusInt(oldDailyPig.getSowEnd(), changeDto.getRemoveCountChange()));
        } else {

            //2.公猪
            if (Objects.equals(changeDto.getChangeTypeId(), SALE)) {
                oldDailyPig.setBoarSale(EventUtil.plusInt(oldDailyPig.getBoarSale(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), DEAD)){
                oldDailyPig.setBoarDead(EventUtil.plusInt(oldDailyPig.getBoarDead(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), WEED)) {
                oldDailyPig.setBoarWeedOut(EventUtil.plusInt(oldDailyPig.getBoarWeedOut(), changeDto.getRemoveCountChange()));
            } else {
                oldDailyPig.setBoarOtherOut(EventUtil.plusInt(oldDailyPig.getBoarOtherOut(), changeDto.getRemoveCountChange()));
            }
            oldDailyPig.setBoarEnd(EventUtil.minusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
        }
        return oldDailyPig;
    }

    /**
     * 更新非生产天数
     * @param removalEvent 离场事件
     */
    private void updateNpd(DoctorPigEvent removalEvent) {
        //最近一次配种事件
        DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(removalEvent.getPigId(), removalEvent.getParity());
        if (lastMate == null) {
            return;
        }

        DateTime mattingDate = new DateTime(lastMate.getEventAt());
        DateTime eventTime = new DateTime(removalEvent.getEventAt());
        int npd = Math.abs(Days.daysBetween(eventTime, mattingDate).getDays());

        //1.死亡
        if (Objects.equals(removalEvent.getChangeTypeId(), DoctorBasicEnums.DEAD.getId())) {
            removalEvent.setPsnpd(removalEvent.getPsnpd() + npd);
            removalEvent.setNpd(removalEvent.getNpd() + npd);
        }

        //2.淘汰
        if (Objects.equals(removalEvent.getChangeTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            removalEvent.setPtnpd(removalEvent.getPtnpd() + npd);
            removalEvent.setNpd(removalEvent.getNpd() + npd);
        }
    }
}
