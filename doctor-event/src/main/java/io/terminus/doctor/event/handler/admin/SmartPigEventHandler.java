package io.terminus.doctor.event.handler.admin;

import io.terminus.doctor.event.handler.PigEventBuilder;
import io.terminus.doctor.event.handler.PigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/10/9.
 */
@Component
public class SmartPigEventHandler implements PigEventHandler, ApplicationContextAware {

    private ApplicationContext ac;
    private List<PigEventHandler> handlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
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
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return handlers.stream().anyMatch(pigEventHandler -> pigEventHandler.isSupportedEvent(pigEvent));
    }

    @Override
    public void handle(DoctorPigEvent pigEvent) {
        handlers.stream().forEach(pigEventHandler -> {
            if (pigEventHandler.isSupportedEvent(pigEvent))
                pigEventHandler.handle(pigEvent);
        });
    }
}
