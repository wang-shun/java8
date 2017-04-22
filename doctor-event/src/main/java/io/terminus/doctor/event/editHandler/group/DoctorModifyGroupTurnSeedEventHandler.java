package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEntryEventHandler;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 10:45 2017/4/15
 */
@Component
public class DoctorModifyGroupTurnSeedEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Autowired
    private DoctorModifyPigEntryEventHandler doctorModifyPigEntryEventHandler;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorTurnSeedGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorTurnSeedGroupInput.class);
        DoctorTurnSeedGroupInput newInput = (DoctorTurnSeedGroupInput) input;
        return DoctorEventChangeDto.builder()
                .farmId(oldGroupEvent.getFarmId())
                .businessId(oldGroupEvent.getGroupId())
                .oldEventAt(oldGroupEvent.getEventAt())
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldPigCode(oldInput.getPigCode())
                .pigCode(newInput.getPigCode())
                .weightChange(newInput.getWeight() - oldInput.getWeight())
                .build();
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            updateDailyOfDelete(oldGroupEvent);
            updateDailyOfNew(oldGroupEvent, input);
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorGroupEvent newEvent) {
        DoctorPigEvent entryEvent = doctorPigEventDao.findByRelGroupEventId(newEvent.getId());
        doctorModifyPigEntryEventHandler.modifyHandle(entryEvent, buildTriggerEventInput(newEvent));
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), 1));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        updateDailyOfDelete(deleteGroupEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder().quantityChange(-1).build();
        DoctorDailyGroup oldDailyGroup1 = doctorDailyGroupDao.findByGroupIdAndSumAt(oldGroupEvent.getGroupId(), oldGroupEvent.getEventAt());
        doctorDailyGroupDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(oldGroupEvent.getGroupId(), getAfterDay(oldGroupEvent.getEventAt()), -changeDto1.getQuantityChange());

    }

    @Override
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {
        Date eventAt = DateUtil.toDate(input.getEventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder().quantityChange(1).build();
        DoctorDailyGroup oldDailyGroup2 = doctorDailyReportManager.findByGroupIdAndSumAt(newGroupEvent.getGroupId(), eventAt);
        doctorDailyReportManager.createOrUpdateDailyGroup(buildDailyGroup(oldDailyGroup2, changeDto2));
        updateDailyGroupLiveStock(newGroupEvent.getGroupId(), getAfterDay(eventAt), changeDto2.getQuantityChange());

    }

    private DoctorDailyGroup buildDailyGroup(DoctorDailyGroup oldDailyGroup, DoctorEventChangeDto changeDto) {
        oldDailyGroup.setOuterOut(EventUtil.plusInt(oldDailyGroup.getOuterOut(), changeDto.getQuantityChange()));
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }

    private DoctorFarmEntryDto buildTriggerEventInput(DoctorGroupEvent turnSeedEvent) {
        DoctorFarmEntryDto farmEntryDto = new DoctorFarmEntryDto();
        DoctorTurnSeedGroupInput input = JSON_MAPPER.fromJson(turnSeedEvent.getExtra(), DoctorTurnSeedGroupInput.class);
        DoctorBarn entryBarn = doctorBarnDao.findById(input.getToBarnId());

        //公母猪进场字段
        if (Objects.equals(getSex(entryBarn.getPigType()), DoctorPig.PigSex.BOAR)) {
            farmEntryDto.setPigType(DoctorPig.PigSex.BOAR.getKey());
            farmEntryDto.setBoarType(BoarEntryType.HGZ.getKey());
            farmEntryDto.setBoarTypeName(BoarEntryType.HGZ.getCode());
        } else {
            farmEntryDto.setPigType(DoctorPig.PigSex.SOW.getKey());
            farmEntryDto.setParity(1);
            farmEntryDto.setEarCode(input.getEarCode());
        }

        farmEntryDto.setRelGroupEventId(input.getRelGroupEventId());
        farmEntryDto.setPigCode(input.getPigCode());
        farmEntryDto.setBarnId(input.getToBarnId());
        farmEntryDto.setBarnName(input.getToBarnName());
        farmEntryDto.setEventType(PigEvent.ENTRY.getKey());
        farmEntryDto.setEventName(PigEvent.ENTRY.getName());
        farmEntryDto.setEventDesc(PigEvent.ENTRY.getDesc());
        farmEntryDto.setIsAuto(IsOrNot.YES.getValue());

        //进场信息
        farmEntryDto.setPigCode(input.getPigCode());
        farmEntryDto.setBirthday(DateUtil.toDate(input.getBirthDate()));
        farmEntryDto.setInFarmDate(DateUtil.toDate(input.getEventAt()));
        farmEntryDto.setSource(PigSource.LOCAL.getKey());
        farmEntryDto.setBreed(input.getBreedId());
        farmEntryDto.setBreedName(input.getBreedName());
        farmEntryDto.setBreedType(input.getGeneticId());
        farmEntryDto.setBreedTypeName(input.getGeneticName());
        farmEntryDto.setMotherCode(input.getMotherEarCode());
        farmEntryDto.setEarCode(input.getEarCode());
        farmEntryDto.setWeight(input.getWeight());
        return farmEntryDto;
    }

    /**
     * 根据转入猪舍获取转种猪性别
     * @param toBarnType 猪舍类型
     * @return 猪性别
     */
    private static DoctorPig.PigSex getSex(Integer toBarnType) {
        if (PigType.MATING_TYPES.contains(toBarnType)) {
            return DoctorPig.PigSex.SOW;
        }
        return DoctorPig.PigSex.BOAR;
    }
}
