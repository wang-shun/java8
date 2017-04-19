package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 17/4/17.
 */
@Component
public class DoctorModifyPigPigletsChgHandler extends DoctorAbstractModifyPigEventHandler{
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigletsChgDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorPigletsChgDto.class);
        DoctorPigletsChgDto newDto = (DoctorPigletsChgDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .changeTypeId(newDto.getPigletsChangeType())
                .oldChangeTypeId(oldDto.getPigletsChangeType())
                .quantityChange(EventUtil.minusInt(newDto.getPigletsCount(), oldDto.getPigletsCount()))
                .weightChange(EventUtil.minusDouble(newDto.getPigletsWeight(), oldDto.getPigletsWeight()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorPigletsChgDto newDto = (DoctorPigletsChgDto) inputDto;
        newEvent.setQuantity(newDto.getPigletsCount());
        newEvent.setChangeTypeId(newDto.getPigletsChangeType());
        newEvent.setQuantity(newDto.getPigletsCount());
        newEvent.setWeight(newDto.getPigletsWeight());
        newEvent.setCustomerId(newDto.getPigletsCustomerId());
        newEvent.setCustomerName(newDto.getPigletsCustomerName());
        newEvent.setPrice(newDto.getPigletsPrice());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            return oldPigTrack;
        }
        oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), changeDto.getQuantityChange()));
        return oldPigTrack;
    }
}
