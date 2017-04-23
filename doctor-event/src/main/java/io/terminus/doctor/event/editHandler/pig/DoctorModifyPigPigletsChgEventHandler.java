package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupChangeEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 17/4/17.
 * 仔猪变动
 */
@Component
public class DoctorModifyPigPigletsChgEventHandler extends DoctorAbstractModifyPigEventHandler{
    @Autowired
    private DoctorModifyGroupChangeEventHandler doctorModifyGroupChangeEventHandler;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigletsChgDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorPigletsChgDto.class);
        DoctorPigletsChgDto newDto = (DoctorPigletsChgDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .newChangeTypeId(newDto.getPigletsChangeType())
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
        oldPigTrack.setUnweanQty(EventUtil.minusInt(oldPigTrack.getUnweanQty(), changeDto.getQuantityChange()));
        return oldPigTrack;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorGroupEvent changeGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.CHANGE.getValue());
        doctorModifyGroupChangeEventHandler.modifyHandle(changeGroupEvent, buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent changeGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.CHANGE.getValue());
        doctorModifyGroupChangeEventHandler.rollbackHandle(changeGroupEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), deletePigEvent.getQuantity()));
        return oldPigTrack;
    }

    public BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent) {
        DoctorPigletsChgDto dto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorPigletsChgDto.class);
        DoctorChangeGroupInput doctorChangeGroupInput = new DoctorChangeGroupInput();
        doctorChangeGroupInput.setSowCode(pigEvent.getPigCode());
        doctorChangeGroupInput.setSowId(pigEvent.getPigId());
        doctorChangeGroupInput.setEventType(GroupEventType.CHANGE.getValue());
        doctorChangeGroupInput.setEventAt(DateUtil.toDateString(dto.getPigletsChangeDate()));
        doctorChangeGroupInput.setChangeTypeId(dto.getPigletsChangeType());             //变动类型id
        doctorChangeGroupInput.setChangeTypeName(dto.getPigletsChangeTypeName());       //变动类型名称
        doctorChangeGroupInput.setChangeReasonId(dto.getPigletsChangeReason());         //变动原因id
        doctorChangeGroupInput.setChangeReasonName(dto.getPigletsChangeReasonName());   //变动原因名称
        doctorChangeGroupInput.setQuantity(dto.getPigletsCount());                      //变动仔猪数量
        doctorChangeGroupInput.setSowQty(dto.getSowPigletsCount());
        doctorChangeGroupInput.setBoarQty(dto.getBoarPigletsCount());
        doctorChangeGroupInput.setWeight(dto.getPigletsWeight());
        doctorChangeGroupInput.setPrice(dto.getPigletsPrice());                             //单价
        if (dto.getPigletsPrice() != null) {
            doctorChangeGroupInput.setAmount(dto.getPigletsPrice() * dto.getPigletsCount());    //总额
        }
        doctorChangeGroupInput.setCustomerId(dto.getPigletsCustomerId());
        doctorChangeGroupInput.setRemark(dto.getPigletsMark());
        doctorChangeGroupInput.setIsAuto(IsOrNot.YES.getValue());           //自动生成事件标识
        doctorChangeGroupInput.setCreatorId(pigEvent.getOperatorId());
        doctorChangeGroupInput.setCreatorName(pigEvent.getOperatorName());
        doctorChangeGroupInput.setRelPigEventId(pigEvent.getId());        //猪事件id
        doctorChangeGroupInput.setSowEvent(true);   //母猪触发的变动事件
        return doctorChangeGroupInput;
    }

}
