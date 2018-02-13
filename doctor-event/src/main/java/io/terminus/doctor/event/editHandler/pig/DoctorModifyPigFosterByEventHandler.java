package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 17/4/19.
 * 被拼窝
 */
@Component
public class DoctorModifyPigFosterByEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        return false;
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFosterByDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFosterByDto.class);
        DoctorFosterByDto newDto = (DoctorFosterByDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldDto.eventAt())
                .newEventAt(newDto.eventAt())
                .quantityChange(EventUtil.minusInt(newDto.getFosterByCount(), oldDto.getFosterByCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFosterByDto newDto = (DoctorFosterByDto) inputDto;
        newEvent.setWeight(newDto.getFosterByTotalWeight());
        newEvent.setQuantity(newDto.getFosterByCount());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        return oldPigTrack;
    }

}
