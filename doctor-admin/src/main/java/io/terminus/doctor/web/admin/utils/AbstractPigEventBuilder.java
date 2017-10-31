package io.terminus.doctor.web.admin.utils;


import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.PigEventBuilder;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.parana.user.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.lang.reflect.ParameterizedType;
import java.util.Locale;

import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
public abstract class AbstractPigEventBuilder<T extends BasePigEventInputDto> implements PigEventBuilder {

    protected static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;

    @Autowired
    private MessageSource messageSource;

    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;

    protected T parse(String eventDto, Class<T> clazz) {
        return jsonMapper.fromJson(eventDto, clazz);
    }


    protected void transfer(T eventDto, DoctorPigEvent pigEvent) {

        BeanMapper.copy(eventDto, pigEvent);
        pigEvent.setRemark(eventDto.changeRemark());
        pigEvent.setIsAuto(IsOrNot.NO.getValue());
        pigEvent.setEventAt(eventDto.eventAt());

        pigEvent.setExtra(jsonMapper.toJson(eventDto));
    }


    @Override
    public void buildEvent(String eventDto, DoctorPigEvent pigEvent) {
        Class<T> clazz = getEventDtoClass();
        T event = parse(eventDto, clazz);
        if (null != event.getOperatorId() && null == event.getOperatorName()) {
            UserProfile profile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(event.getOperatorId()));
            if (null != profile)
                event.setOperatorName(profile.getRealName());
        }
        PigEvent eventType = PigEvent.from(pigEvent.getType());
        event.setEventType(eventType.getKey());
        event.setEventName(eventType.getName());
        event.setEventDesc(eventType.getDesc());
        event.setPigId(pigEvent.getPigId());
        event.setPigCode(pigEvent.getPigCode());
        try {
            buildEventDto(event, pigEvent);
        } catch (InvalidException e) {
            throw new ServiceException(messageSource.getMessage(e.getError(), e.getParams(), Locale.CHINA));
        }

        transfer(event, pigEvent);
    }


    private Class<T> getEventDtoClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 从eventDto构建pigEvent
     * 同名的字段不需要处理，自动转移
     *
     * @param eventDto
     * @param pigEvent
     */
    abstract void buildEventDto(T eventDto, DoctorPigEvent pigEvent);

}
