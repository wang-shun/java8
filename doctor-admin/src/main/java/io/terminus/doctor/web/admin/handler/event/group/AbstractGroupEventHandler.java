package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.web.admin.handler.event.AbstractEventHandler;
import io.terminus.doctor.web.admin.utils.GroupEventHandler;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
public abstract class AbstractGroupEventHandler<T extends BaseGroupInput> extends AbstractEventHandler<T, DoctorGroupEvent> implements GroupEventHandler {


    @Override
    public void updateEvent(String eventDto, DoctorGroupEvent groupEvent) {
        Class<T> clazz = getEventDtoClass();
        T event = parse(eventDto, clazz);

        buildEventDto(event, groupEvent);

        super.transfer(event, groupEvent);

        groupEvent.setDesc(event.generateEventDesc());
        groupEvent.setExtra(jsonMapper.toJson(event));
        groupEvent.setEventAt(DateUtil.toDate(event.getEventAt()));
        groupEvent.setRemark(event.getRemark());
    }

}
