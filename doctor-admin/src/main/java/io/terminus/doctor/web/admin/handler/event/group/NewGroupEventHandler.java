package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * 新建猪群
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class NewGroupEventHandler extends AbstractGroupEventHandler<DoctorNewGroupInput> {

    @Override
    protected void buildEventDto(DoctorNewGroupInput eventDto, DoctorGroupEvent groupEvent) {

    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.NEW.getValue();
    }
}
