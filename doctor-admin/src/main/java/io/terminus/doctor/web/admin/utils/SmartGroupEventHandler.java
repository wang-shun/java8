package io.terminus.doctor.web.admin.utils;



import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class SmartGroupEventHandler implements GroupEventHandler, ApplicationContextAware {

    private ApplicationContext ac;
    private List<GroupEventHandler> handlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    @PostConstruct
    public void init() {
        if (null == handlers || handlers.isEmpty()) {
            Map<String, GroupEventHandler> handlerBeans = ac.getBeansOfType(GroupEventHandler.class);
            for (String beanName : handlerBeans.keySet()) {
                if (beanName.equals("smartGroupEventHandler"))
                    continue;
                handlers.add(handlerBeans.get(beanName));
            }
        }
    }

    public void registerHandler(GroupEventHandler handler) {
        this.handlers.add(handler);
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return handlers.stream().anyMatch(g -> g.isSupported(groupEvent));
    }

    @Override
    public void updateEvent(String eventDto, DoctorGroupEvent groupEvent) {
        handlers.stream().forEach(g -> {
            if (g.isSupported(groupEvent))
                g.updateEvent(eventDto, groupEvent);
        });
    }
}
