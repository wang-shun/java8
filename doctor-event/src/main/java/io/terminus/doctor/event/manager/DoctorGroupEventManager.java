package io.terminus.doctor.event.manager;

import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorGroupEventManager {

    @Autowired
    private ApplicationContext applicationContext;

    private Map<Class<? extends DoctorGroupEventHandler>, DoctorGroupEventHandler> handlerMapping;

    /**
     * 初始化所有实现的
     */
    @PostConstruct
    public void initHandlers() {
        handlerMapping = Maps.newHashMap();
        Map<String, DoctorGroupEventHandler> handlers = applicationContext.getBeansOfType(DoctorGroupEventHandler.class);
        log.info("Doctor group event handlers :{}", handlers);
        if (!handlers.isEmpty()) {
            handlers.values().forEach(handler -> handlerMapping.put(handler.getClass(), handler));
        }
    }

    /**
     * 事务方式执行猪群事件
     * @param groupDetail  猪群明细
     * @param input        录入信息
     * @param handlerClass 事件handler的实现类
     * @see GroupEventType
     */
    @Transactional
    public <I extends BaseGroupInput>
    void handleEvent(DoctorGroupDetail groupDetail, I input, Class<? extends DoctorGroupEventHandler> handlerClass) {
        getHandler(handlerClass).handle(groupDetail.getGroup(), groupDetail.getGroupTrack(), input);
    }

    /**
     * 获取事件处理器
     * @param interfaceClass 处理的实现类
     * @return 事件处理器
     */
    private DoctorGroupEventHandler getHandler(Class<? extends DoctorGroupEventHandler> interfaceClass) {
        if (!handlerMapping.containsKey(interfaceClass) || handlerMapping.get(interfaceClass) == null) {
            log.error("Not any event handler found for illegal class:{}", interfaceClass.getName());
            throw new ServiceException("handler.not.found");
        }
        return handlerMapping.get(interfaceClass);
    }
}
