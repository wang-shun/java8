package io.terminus.doctor.event.editHandler.pig;

import com.google.common.base.Objects;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.event.dto.DoctorBasicInputInfoDto.generateEventDescFromExtra;

/**
 * 状态新事件的构建
 * Created by terminus on 2017/4/17.
 */
@Component
public class DoctorModifyPigConditionEventHandler extends DoctorAbstractModifyPigEventHandler{

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {

        if (Objects.equal(oldPigEvent.getBarnType(), DoctorPig.PigSex.SOW.getKey())) {
            DoctorConditionDto newConditionDto = (DoctorConditionDto) inputDto;
            DoctorConditionDto oldConditionDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorConditionDto.class);
            return DoctorEventChangeDto.builder()
                    .farmId(oldPigEvent.getFarmId())
                    .businessId(oldPigEvent.getPigId())
                    .newEventAt(newConditionDto.eventAt())
                    .remark(newConditionDto.getConditionRemark())
                    .oldEventAt(oldPigEvent.getEventAt())
                    .weightChange(EventUtil.minusDouble(newConditionDto.getConditionWeight(), oldConditionDto.getConditionWeight()))
                    .build();
        } else {
            DoctorBoarConditionDto newConditionDto = (DoctorBoarConditionDto) inputDto;
            DoctorBoarConditionDto oldConditionDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorBoarConditionDto.class);
            return DoctorEventChangeDto.builder()
                    .farmId(oldPigEvent.getFarmId())
                    .businessId(oldPigEvent.getPigId())
                    .newEventAt(newConditionDto.eventAt())
                    .remark(newConditionDto.getRemark())
                    .oldEventAt(oldPigEvent.getEventAt())
                    .weightChange(EventUtil.minusDouble(newConditionDto.getWeight(), oldConditionDto.getWeight()))
                    .build();
        }


    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = new DoctorPigEvent();
        if (Objects.equal(oldPigEvent.getBarnType(), DoctorPig.PigSex.SOW.getKey())) {
            DoctorConditionDto doctorConditionDto = (DoctorConditionDto) inputDto;
            BeanMapper.copy(oldPigEvent, doctorPigEvent);
            doctorPigEvent.setRemark(doctorConditionDto.getConditionRemark());
            doctorPigEvent.setEventAt(doctorConditionDto.getConditionDate());
            doctorPigEvent.setDesc(generateEventDescFromExtra(doctorConditionDto));
            doctorPigEvent.setExtra(TO_JSON_MAPPER.toJson(doctorConditionDto));
        } else {
            DoctorBoarConditionDto doctorBoarConditionDto = (DoctorBoarConditionDto) inputDto;
            BeanMapper.copy(oldPigEvent, doctorPigEvent);
            doctorPigEvent.setRemark(doctorBoarConditionDto.getRemark());
            doctorPigEvent.setEventAt(doctorBoarConditionDto.eventAt());
            doctorPigEvent.setDesc(generateEventDescFromExtra(doctorBoarConditionDto));
            doctorPigEvent.setExtra(TO_JSON_MAPPER.toJson(doctorBoarConditionDto));
        }
        return doctorPigEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {

        oldPigTrack.setWeight(EventUtil.minusDouble(changeDto.getWeightChange(), oldPigTrack.getWeight()));
        return oldPigTrack;
    }
}
