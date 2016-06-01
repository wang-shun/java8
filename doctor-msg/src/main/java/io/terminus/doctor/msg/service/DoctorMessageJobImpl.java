package io.terminus.doctor.msg.service;

import io.terminus.doctor.msg.producer.IProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Desc: 消息执行job
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Component
public class DoctorMessageJobImpl implements DoctorMessageJob {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void produce() {
        Map<String, IProducer> producerMap = applicationContext.getBeansOfType(IProducer.class);
        if (producerMap != null) {
            producerMap.forEach((beanName, producer) -> producer.produce());
        }
    }
}
