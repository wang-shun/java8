package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class MoveInGroupEventHandler extends AbstractGroupEventHandler<DoctorMoveInGroupInput> {

    @Override
    protected void buildEventDto(DoctorMoveInGroupInput eventDto, DoctorGroupEvent groupEvent) {

        eventDto.setBreedName(getBasicName(eventDto.getBreedId()));
        eventDto.setInTypeName(InType.from(eventDto.getInType()).getDesc());

        groupEvent.setWeight(EventUtil.getWeight(groupEvent.getAvgWeight(), groupEvent.getQuantity()));

    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.MOVE_IN.getValue();
    }
}
