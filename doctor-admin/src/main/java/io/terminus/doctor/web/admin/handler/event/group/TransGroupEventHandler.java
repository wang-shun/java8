package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.springframework.stereotype.Component;

/**
 * 猪群转群
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class TransGroupEventHandler extends AbstractGroupEventHandler<DoctorTransGroupInput> {

    @Override
    protected void buildEventDto(DoctorTransGroupInput eventDto, DoctorGroupEvent groupEvent) {
        eventDto.setToBarnName(getBarnName(eventDto.getToBarnId()));

//        eventDto.setBreedName(getBasicName(eventDto.getBreedId()));

        if (eventDto.getIsCreateGroup().intValue() == IsOrNot.NO.getValue() && null != eventDto.getToGroupId()) {
            eventDto.setToGroupCode(getGroupCode(eventDto.getToGroupId()));
        }

        eventDto.setFcrFeed(getFcrFeed(groupEvent.getGroupId()));
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.TRANS_GROUP.getValue();
    }
}
