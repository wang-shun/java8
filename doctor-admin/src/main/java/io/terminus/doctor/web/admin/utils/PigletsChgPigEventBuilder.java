package io.terminus.doctor.web.admin.utils;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * 仔猪变动
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class PigletsChgPigEventBuilder extends AbstractPigEventBuilder<DoctorPigletsChgDto> {

    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorBasicWriteService doctorBasicWriteService;

    @Override
    void buildEventDto(DoctorPigletsChgDto eventDto, DoctorPigEvent pigEvent) {

        if (DoctorBasicEnums.SALE.getId() == eventDto.getPigletsChangeType().longValue()) {
            if (eventDto.getPigletsPrice() == null)
                throw new ServiceException("sale.price.not.null");
            if (eventDto.getPigletsCustomerName() == null)
                throw new ServiceException("sale.customer.not.null");
        }
        if (notNull(eventDto.getPigletsChangeReason())) {
            DoctorChangeReason changeReason = RespHelper.orServEx(doctorBasicReadService.findChangeReasonById(eventDto.getPigletsChangeReason()));
            if (null == changeReason)
                throw new ServiceException("changeReason.not.found");
            eventDto.setPigletsChangeReasonName(changeReason.getReason());
        }

        DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicById(eventDto.getPigletsChangeType()));

        expectTrue(notNull(doctorBasic), "basic.not.null", eventDto.getPigletsChangeType());
        eventDto.setPigletsChangeTypeName(doctorBasic.getName());
        //新录入的客户要创建一把
        DoctorFarm doctorFarm1 = RespHelper.or500(doctorFarmReadService.findFarmById(pigEvent.getFarmId()));
        expectTrue(notNull(doctorFarm1), "farm.not.null", pigEvent.getFarmId());

        BaseUser currentUser = UserUtil.getCurrentUser();
        if (null == currentUser)
            throw new ServiceException("user.not.login");

        Long customerId = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorFarm1.getId(),
                doctorFarm1.getName(), eventDto.getPigletsCustomerId(), eventDto.getPigletsCustomerName(),
                UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
        eventDto.setPigletsCustomerId(customerId);

        pigEvent.setChangeTypeId(eventDto.getPigletsChangeType());
        pigEvent.setQuantity(eventDto.getPigletsCount());
        pigEvent.setWeight(eventDto.getPigletsWeight());
        pigEvent.setCustomerId(eventDto.getPigletsCustomerId());
        pigEvent.setCustomerName(eventDto.getPigletsCustomerName());
        pigEvent.setPrice(eventDto.getPigletsPrice());
        pigEvent.setAmount(eventDto.getPigletsSum());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.PIGLETS_CHG.getKey().intValue();
    }
}
