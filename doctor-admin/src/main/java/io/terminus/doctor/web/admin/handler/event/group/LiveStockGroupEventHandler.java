package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

/**
 * 猪只存栏
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class LiveStockGroupEventHandler extends AbstractGroupEventHandler<DoctorLiveStockGroupInput> {

    @Override
    protected void buildEventDto(DoctorLiveStockGroupInput eventDto, DoctorGroupEvent groupEvent) {
        groupEvent.setWeight(EventUtil.get2(EventUtil.getWeight(eventDto.getAvgWeight(), groupEvent.getQuantity())));
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.LIVE_STOCK.getValue();
    }
}
