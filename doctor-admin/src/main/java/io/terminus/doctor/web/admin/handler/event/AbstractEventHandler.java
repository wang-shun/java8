package io.terminus.doctor.web.admin.handler.event;

import io.terminus.common.utils.JsonMapper;
import org.springframework.beans.BeanUtils;

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
        Class<T> result = (Class<T>) eventDtoClassCache.putIfAbsent(this.getClass(), (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        if (null == result)
            return (Class<T>) eventDtoClassCache.get(this.getClass());
        return result;
//        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected T parse(String eventDto, Class<T> clazz) {
        return jsonMapper.fromJson(eventDto, clazz);
    }

    protected void transfer(T eventDto, R event) {
        //TODO from string to date not work,use spring beanutils

//        BeanMapper.copy(eventDto, event);
        BeanUtils.copyProperties(eventDto, event);
    }

    protected abstract void buildEventDto(T eventDto, R groupEvent);
}
