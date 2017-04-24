package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/23.
 * 存栏
 */
@Component
public class DoctorModifyGroupLiveStockEventHandler extends DoctorAbstractModifyGroupEventHandler{
    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorLiveStockGroupInput newInput = (DoctorLiveStockGroupInput) input;
        newEvent.setAvgWeight(newInput.getAvgWeight());
        newEvent.setWeight(EventUtil.get2(EventUtil.getWeight(oldGroupEvent.getAvgWeight(), oldGroupEvent.getQuantity())));
        return newEvent;
    }
}
