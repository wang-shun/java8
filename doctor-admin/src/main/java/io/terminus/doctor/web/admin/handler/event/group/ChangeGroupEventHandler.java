package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.pampas.common.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.common.utils.RespHelper.orServEx;

/**
 * 猪群变动
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class ChangeGroupEventHandler extends AbstractGroupEventHandler<DoctorChangeGroupInput> {

    @RpcConsumer
    private DoctorBasicWriteService doctorBasicWriteService;

    @Override
    protected void buildEventDto(DoctorChangeGroupInput eventDto, DoctorGroupEvent groupEvent) {

        eventDto.setChangeTypeName(getBasicName(eventDto.getChangeTypeId()));

        DoctorChangeReason changeReason = RespHelper.orServEx(doctorBasicReadService.findChangeReasonById(eventDto.getChangeReasonId()));
        if (null == changeReason)
            throw new InvalidException("basic.not.null", eventDto.getChangeReasonId());
        eventDto.setChangeReasonName(changeReason.getReason());
        if (StringUtils.isBlank(eventDto.getCustomerName())) {
            DoctorCustomer customer = RespHelper.orServEx(doctorBasicReadService.findCustomerById(eventDto.getCustomerId()));
            if (null == customer)
                throw new InvalidException("basic.not.null", eventDto.getCustomerId());
            eventDto.setCustomerName(customer.getName());
        }
        Long customerId = orServEx(doctorBasicWriteService.addCustomerWhenInput(groupEvent.getFarmId(),
                groupEvent.getFarmName(), eventDto.getCustomerId(), eventDto.getCustomerName(),
                UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
        eventDto.setCustomerId(customerId);
        eventDto.setFcrFeed(getFcrFeed(groupEvent.getGroupId()));
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return groupEvent.getType().intValue() == GroupEventType.CHANGE.getValue();
    }
}
