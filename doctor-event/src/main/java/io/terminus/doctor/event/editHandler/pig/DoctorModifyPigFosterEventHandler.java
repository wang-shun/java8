package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/4/19.
 * 拼窝
 */
@Component
public class DoctorModifyPigFosterEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Autowired
    private DoctorModifyPigFosterByEventHandler doctorModifyPigFosterByEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        super.modifyHandleCheck(oldPigEvent, inputDto);
        DoctorFostersDto fostersDto = (DoctorFostersDto) inputDto;
        //当前胎次下有断奶事件,不允许编辑分娩的活仔数
        expectTrue(Objects.equals(fostersDto.getFostersCount(), oldPigEvent.getQuantity())
                        || isNull(doctorPigEventDao.getWeanEventByParity(oldPigEvent.getPigId(), oldPigEvent.getParity()))
                , "current.parity.has.wean");
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFostersDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFostersDto.class);
        DoctorFostersDto newDto = (DoctorFostersDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldDto.eventAt())
                .newEventAt(newDto.eventAt())
                .quantityChange(EventUtil.minusInt(newDto.getFostersCount(), oldDto.getFostersCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFostersDto newDto = (DoctorFostersDto) inputDto;
        newEvent.setQuantity(newDto.getFostersCount());
        newEvent.setWeight(newDto.getFosterTotalWeight());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            oldPigTrack.setUnweanQty(EventUtil.minusInt(oldPigTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        return oldPigTrack;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorPigEvent fosterByEvent = doctorPigEventDao.findByRelPigEventId(newPigEvent.getId());
        doctorModifyPigFosterByEventHandler.modifyHandle(fosterByEvent, buildPigInputDto(newPigEvent));
    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        return false;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        super.triggerEventRollbackHandle(deletePigEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        if (Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), deletePigEvent.getQuantity()));
        }
        return oldPigTrack;
    }

    public BasePigEventInputDto buildPigInputDto(DoctorPigEvent doctorPigEvent) {
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(doctorPigEvent.getExtra(), DoctorFostersDto.class);

        DoctorFosterByDto fosterByDto = DoctorFosterByDto.builder()
                .fromSowId(fostersDto.getPigId())
                .fromSowCode(fostersDto.getPigCode())
                .fosterByDate(DateUtil.toDate(fostersDto.getFostersDate()))
                .fosterByCount(fostersDto.getFostersCount())
                .boarFostersByCount(fostersDto.getBoarFostersCount())
                .sowFostersByCount(fostersDto.getSowFostersCount())
                .fosterByTotalWeight(fostersDto.getFosterTotalWeight())
                .build();

        fosterByDto.setRelPigEventId(doctorPigEvent.getId());
        fosterByDto.setEventName(PigEvent.FOSTERS_BY.getName());
        fosterByDto.setEventType(PigEvent.FOSTERS_BY.getKey());
        fosterByDto.setEventDesc(PigEvent.FOSTERS_BY.getDesc());

        return fosterByDto;
    }

}
