package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.doctor.msg.producer.IProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Desc: 消息执行job
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Component
@Slf4j
public class DoctorMessageJobImpl implements DoctorMessageJob {

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, IProducer> producerMap = applicationContext.getBeansOfType(IProducer.class);
        System.out.println(producerMap);
    }

    @Override
    @Transactional
    public void produce() {
        try{
            Map<String, IProducer> producerMap = applicationContext.getBeansOfType(IProducer.class);
            if (producerMap != null) {
                producerMap.forEach((beanName, producer) -> producer.produce());
            }
        } catch (Exception e) {
            log.error("[produce message] -> message produce error", Throwables.getStackTraceAsString(e));
        }
    }
}
