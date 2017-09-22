package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.admin.handler.event.AbstractEventHandler;
import io.terminus.doctor.web.admin.utils.GroupEventHandler;
import io.terminus.parana.user.model.UserProfile;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
public abstract class AbstractGroupEventHandler<T extends BaseGroupInput> extends AbstractEventHandler<T, DoctorGroupEvent> implements GroupEventHandler {

    @RpcConsumer
    protected DoctorBasicReadService doctorBasicReadService;
    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;

    @RpcConsumer
    protected DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @RpcConsumer
    protected DoctorGroupReadService doctorGroupReadService;
    @RpcConsumer
    protected DoctorUserProfileReadService doctorUserProfileReadService;

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

    protected String getBasicName(Long id) {
        DoctorBasic basic = RespHelper.orServEx(doctorBasicReadService.findBasicById(id));
        if (null == basic)
            throw new InvalidException("basic.not.null", id);
        return basic.getName();
    }

    protected String getBarnName(Long barnId) {
        DoctorBarn toBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(barnId));
        if (null == toBarn)
            throw new InvalidException("barn.not.null", barnId);
        return toBarn.getName();
    }

    protected Double getFcrFeed(Long groupId) {
        return RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, groupId, null, null), 0D);
    }

    protected String getGroupCode(Long groupId) {
        DoctorGroup group = RespHelper.orServEx(doctorGroupReadService.findGroupById(groupId));
        if (null == group) throw new InvalidException("event.group.not.found", groupId);
        return group.getGroupCode();
    }

    protected String getName(Long userId) {
        UserProfile user = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(userId));
        if (null == user)
            throw new InvalidException("user.not.found", userId);
        return user.getRealName();
    }
}
