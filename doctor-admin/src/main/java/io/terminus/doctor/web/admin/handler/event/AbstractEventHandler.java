package io.terminus.doctor.web.admin.handler.event;

import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ConcurrentHashMap;

import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
public abstract class AbstractEventHandler<T, R> {

    protected static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;

    private static ConcurrentHashMap<Class<? extends AbstractEventHandler>, Class<?>> eventDtoClassCache = new ConcurrentHashMap();

    protected Class<T> getEventDtoClass() {
        return (Class<T>) eventDtoClassCache.putIfAbsent(this.getClass(), (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
//        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected T parse(String eventDto, Class<T> clazz) {
        return jsonMapper.fromJson(eventDto, clazz);
    }

    protected void transfer(T eventDto, R event) {
        BeanMapper.copy(eventDto, event);
    }

    protected abstract void buildEventDto(T eventDto, R groupEvent);
}
