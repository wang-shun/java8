package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/21.
 * 离场
 */
@Component
public class DoctorModifyPigRemoveEventHandler extends DoctorAbstractModifyPigEventHandler {
    private final  Map<Integer, Integer> EVENT_TO_STATUS = Maps.newHashMap();

    {
        EVENT_TO_STATUS.put(PigEvent.ENTRY.getKey(), PigStatus.Entry.getKey());
        EVENT_TO_STATUS.put(PigEvent.MATING.getKey(), PigStatus.Mate.getKey());
        EVENT_TO_STATUS.put(PigEvent.TO_FARROWING.getKey(), PigStatus.Farrow.getKey());
        EVENT_TO_STATUS.put(PigEvent.FARROWING.getKey(), PigStatus.FEED.getKey());
        EVENT_TO_STATUS.put(PigEvent.WEAN.getKey(), PigStatus.Wean.getKey());
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
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())
                && !Objects.equals(changeDto.getNewChangeTypeId(), changeDto.getOldChangeTypeId())) {
            DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
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
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto2));
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
        DoctorPigEvent beforeStatusEvent = doctorPigEventDao.getLastStatusEventBeforeEventAt(deletePigEvent.getPigId(), deletePigEvent.getEventAt());
        oldPigTrack.setStatus(getStatus(beforeStatusEvent));
        oldPigTrack.setIsRemoval(IsOrNot.NO.getValue());
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
                .pigSex(oldPigEvent.getKind())
                .changeTypeId(oldPigEvent.getChangeTypeId())
                .removeCountChange(-1)
                .barnType(oldPigEvent.getBarnType())
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));
        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorDailyPigDao.updateDailySowPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()), changeDto1.getRemoveCountChange());
        } else {
            doctorDailyPigDao.updateDailyBoarPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()), changeDto1.getRemoveCountChange());
        }
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorRemovalDto newDto = (DoctorRemovalDto) inputDto;
        DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), newDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pigSex(newPigEvent.getKind())
                .changeTypeId(newDto.getChgTypeId())
                .removeCountChange(1)
                .barnType(newPigEvent.getBarnType())
                .build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorDailyPigDao.updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()), -changeDto2.getRemoveCountChange());
        } else {
            doctorDailyPigDao.updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(newDto.eventAt()), -changeDto2.getRemoveCountChange());
        }
    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
            if (Objects.equals(changeDto.getChangeTypeId(), 109L)) { //销售
                oldDailyPig.setSowSale(EventUtil.plusInt(oldDailyPig.getSowSale(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), 110L)){ //死亡
                oldDailyPig.setSowDead(EventUtil.plusInt(oldDailyPig.getSowDead(), changeDto.getRemoveCountChange()));
            } else if (Objects.equals(changeDto.getChangeTypeId(), 111L)) { //淘汰
                oldDailyPig.setSowWeedOut(EventUtil.plusInt(oldDailyPig.getSowWeedOut(), changeDto.getRemoveCountChange()));
            } else {
                oldDailyPig.setSowOtherOut(EventUtil.plusInt(oldDailyPig.getSowOtherOut(), changeDto.getRemoveCountChange()));
            }

            if (Objects.equals(changeDto.getBarnType(), PigType.DELIVER_SOW.getValue())) {
                oldDailyPig.setSowCf(EventUtil.minusInt(oldDailyPig.getSowCf(), changeDto.getRemoveCountChange()));
            } else {
                oldDailyPig.setSowPh(EventUtil.minusInt(oldDailyPig.getSowPh(), changeDto.getRemoveCountChange()));
            }
            oldDailyPig.setSowIn(EventUtil.plusInt(oldDailyPig.getSowIn(), changeDto.getRemoveCountChange()));
            oldDailyPig.setSowEnd(EventUtil.plusInt(oldDailyPig.getSowEnd(), changeDto.getRemoveCountChange()));
        } else {
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getRemoveCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getRemoveCountChange()));
        }
        return oldDailyPig;
    }

    /**
     * 更新非生产天数
     * @param removalEvent 离场事件
     */
    private void updateNpd(DoctorPigEvent removalEvent){
            //如果是死亡 或者淘汰,查找最近一次配种事件
            DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(removalEvent.getPigId(), removalEvent.getParity());
            if (lastMate == null) {
                return;
            }
            DateTime mattingDate = new DateTime(lastMate.getEventAt());
            DateTime eventTime = new DateTime(removalEvent.getEventAt());

            int npd = Math.abs(Days.daysBetween(eventTime, mattingDate).getDays());
            if (Objects.equals(removalEvent.getChangeTypeId(), DoctorBasicEnums.DEAD.getId())) {
                //如果是死亡
                removalEvent.setPsnpd(removalEvent.getPsnpd() + npd);
                removalEvent.setNpd(removalEvent.getNpd() + npd);
            }
            if (Objects.equals(removalEvent.getChangeTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
                //如果是淘汰
                removalEvent.setPtnpd(removalEvent.getPtnpd() + npd);
                removalEvent.setNpd(removalEvent.getNpd() + npd);
            }
    }

    private Integer getStatus(DoctorPigEvent pigEvent) {
        if (Objects.equals(pigEvent.getType(), PigEvent.PREG_CHECK.getKey())) {
            return Objects.equals(pigEvent.getPregCheckResult(), PregCheckResult.YANG.getKey())
                    ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey();
        }
        return EVENT_TO_STATUS.get(pigEvent.getType());
    }
}
