package io.terminus.doctor.web.admin.utils;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class RemovalPigEventBuilder extends AbstractPigEventBuilder<DoctorRemovalDto> {

    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorBasicWriteService doctorBasicWriteService;
    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;

    @Override
    void buildEventDto(DoctorRemovalDto eventDto, DoctorPigEvent pigEvent) {

        if (null != eventDto.getSum())
            eventDto.setSum(eventDto.getSum() * 100);

        if (eventDto.getChgTypeId().longValue() == DoctorBasicEnums.SALE.getId() && eventDto.getPrice() == null) {
            throw new ServiceException("sale.price.not.null");
        }

        pigEvent.setChangeTypeId(eventDto.getChgTypeId());
        pigEvent.setAmount(EventUtil.getAmount(eventDto.getPrice(), eventDto.getWeight()));

        DoctorBasic chgType = RespHelper.or500(doctorBasicReadService.findBasicById(eventDto.getChgTypeId()));
        if (null == chgType)
            throw new InvalidException("basic.not.null", eventDto.getChgTypeId());
        eventDto.setChgTypeName(chgType.getName());

        if (eventDto.getChgReasonId() != null) {
            DoctorChangeReason changeReason = RespHelper.or500(doctorBasicReadService.findChangeReasonById(eventDto.getChgReasonId()));
            if (null == changeReason)
                throw new InvalidException("change.reason.not.null", eventDto.getChgReasonId());
            eventDto.setChgReasonName(changeReason.getReason());
        }

        DoctorFarm doctorFarm2 = RespHelper.or500(doctorFarmReadService.findFarmById(pigEvent.getFarmId()));
        if (null == doctorFarm2)
            throw new InvalidException("farm.not.null", pigEvent.getFarmId());


        BaseUser currentUser = UserUtil.getCurrentUser();
        if (null == currentUser)
            throw new InvalidException("user.not.login");

        Long customerId1 = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorFarm2.getId(),
                doctorFarm2.getName(), eventDto.getCustomerId(), eventDto.getCustomerName(),
                currentUser.getId(), currentUser.getName()));
        eventDto.setCustomerId(customerId1);


        if (eventDto.getChgTypeId().longValue() == DoctorBasicEnums.DEAD.getId()
                || eventDto.getChgTypeId().longValue() == DoctorBasicEnums.ELIMINATE.getId()) {
            //最近一次配种事件
            DoctorPigEvent lastMate = RespHelper.orServEx(doctorPigEventReadService.findLastFirstMateEvent(pigEvent.getPigId(), pigEvent.getParity()));
            if (lastMate != null) {
                DateTime mattingDate = new DateTime(lastMate.getEventAt());
                DateTime eventTime = new DateTime(pigEvent.getEventAt());
                int npd = Math.abs(Days.daysBetween(eventTime, mattingDate).getDays());

                //1.死亡
                if (DoctorBasicEnums.DEAD.getId() == eventDto.getChgTypeId().longValue()) {
                    pigEvent.setPsnpd(pigEvent.getPsnpd() + npd);
                    pigEvent.setNpd(pigEvent.getNpd() + npd);
                }

                //2.淘汰
                if (DoctorBasicEnums.ELIMINATE.getId() == eventDto.getChgTypeId().longValue()) {
                    pigEvent.setPtnpd(pigEvent.getPtnpd() + npd);
                    pigEvent.setNpd(pigEvent.getNpd() + npd);
                }
            }
        }
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.REMOVAL.getKey().intValue();
    }
}
