package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.handler.PigEventBuilder;
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
 * Created by sunbo@terminus.io on 2017/9/13.
 */
@Component
public class SmartPigEventBuilder implements PigEventBuilder, ApplicationContextAware {


    private ApplicationContext ac;
    private List<PigEventBuilder> builders;

    public SmartPigEventBuilder() {

    }

    @PostConstruct
    public void init() {
        builders = new ArrayList<>();
        Map<String, PigEventBuilder> pigEventBuilders = ac.getBeansOfType(PigEventBuilder.class);
        for (String handlerBeanName : pigEventBuilders.keySet()) {
            if (handlerBeanName.equals("smartPigEventBuilder"))
                continue;
            builders.add(pigEventBuilders.get(handlerBeanName));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    public void registerEventHandler(PigEventBuilder pigEventHandler) {
        this.builders.add(pigEventHandler);
    }


    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return builders.stream().anyMatch(h -> h.isSupportedEvent(pigEvent));
    }

    @Override
    public void buildEvent(String eventDto, DoctorPigEvent pigEvent) {
        builders.stream().forEach(h -> {
            if (h.isSupportedEvent(pigEvent))
                h.buildEvent(eventDto, pigEvent);
        });
    }

}
