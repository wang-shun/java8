package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.handler.PigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
@Component
public class SmartPigEventHandler implements PigEventHandler, ApplicationContextAware {


    private ApplicationContext ac;
    private List<PigEventHandler> handlers;

    public SmartPigEventHandler() {

    }

    @PostConstruct
    public void init() {
        handlers = new ArrayList<>();
        Map<String, PigEventHandler> pigEventHandlers = ac.getBeansOfType(PigEventHandler.class);
        for (String handlerBeanName : pigEventHandlers.keySet()) {
            if (handlerBeanName.equals("smartPigEventHandler"))
                continue;
            handlers.add(pigEventHandlers.get(handlerBeanName));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    public void registerEventHandler(PigEventHandler pigEventHandler) {
        this.handlers.add(pigEventHandler);
    }


    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return handlers.stream().anyMatch(h -> h.isSupportedEvent(pigEvent));
    }

    @Override
    public void updateEvent(String eventDto, DoctorPigEvent pigEvent) {
        handlers.stream().forEach(h -> {
            if (h.isSupportedEvent(pigEvent))
                h.updateEvent(eventDto, pigEvent);
        });
    }
}
