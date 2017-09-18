package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * 商品猪转为种猪
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class TurnSeedGroupEventHandler extends AbstractGroupEventHandler<DoctorTurnSeedGroupInput> {


    @Override
    protected void buildEventDto(DoctorTurnSeedGroupInput eventDto, DoctorGroupEvent groupEvent) {
        eventDto.setBreedName(getBasicName(eventDto.getBreedId()));
        eventDto.setGeneticName(getBasicName(eventDto.getGeneticId()));
        eventDto.setToBarnName(getBarnName(eventDto.getToBarnId()));
        eventDto.setFcrFeed(getFcrFeed(groupEvent.getGroupId()));
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.TURN_SEED.getValue();
    }
}
