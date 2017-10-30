package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.stereotype.Component;

/**
 * 猪群转场
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class TransFarmGroupEventHandler extends AbstractGroupEventHandler<DoctorTransFarmGroupInput> {

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @Override
    protected void buildEventDto(DoctorTransFarmGroupInput eventDto, DoctorGroupEvent groupEvent) {
        eventDto.setBreedName(getBasicName(eventDto.getBreedId()));
        eventDto.setToBarnName(getBarnName(eventDto.getToBarnId()));
        DoctorFarm toFarm = RespHelper.orServEx(doctorFarmReadService.findFarmById(eventDto.getToFarmId()));
        if (null == toFarm)
            throw new InvalidException("farm.not.null", eventDto.getToFarmId());
        eventDto.setToFarmName(toFarm.getName());
        if (eventDto.getIsCreateGroup().intValue() == IsOrNot.NO.getValue()) {
            eventDto.setToGroupCode(getGroupCode(eventDto.getToGroupId()));
        }
        eventDto.setFcrFeed(getFcrFeed(groupEvent.getGroupId()));
        eventDto.setAvgWeight(eventDto.getWeight() / eventDto.getQuantity());

    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.TRANS_FARM.getValue();
    }
}
