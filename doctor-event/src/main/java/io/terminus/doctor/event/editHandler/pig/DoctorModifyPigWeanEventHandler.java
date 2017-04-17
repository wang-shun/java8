package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.handler.sow.DoctorSowWeanHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by xjn on 17/4/17.
 * 断奶编辑回滚处理
 */
@Component
public class DoctorModifyPigWeanEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Autowired
    private DoctorSowWeanHandler doctorSowWeanHandler;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorWeanDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorWeanDto.class);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        return DoctorEventChangeDto.builder()
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .weanCountChange(EventUtil.minusInt(newDto.getWeanPigletsCount(), oldDto.getPartWeanPigletsCount()))
                .weanAvgWeightChange(EventUtil.minusDouble(newDto.getPartWeanAvgWeight(), oldDto.getPartWeanAvgWeight()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        newEvent.setWeanCount(newDto.getPartWeanPigletsCount());
        newEvent.setWeanAvgWeight(newDto.getPartWeanAvgWeight());
        return newEvent;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
    }
}
