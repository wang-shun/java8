package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.doctor.msg.dto.SubUser;
import io.terminus.doctor.msg.producer.IProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
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

    private Map<String, IProducer> producerMap;

    @PostConstruct
    public void init() {
        producerMap = applicationContext.getBeansOfType(IProducer.class);
        if (producerMap == null) {
            producerMap = Maps.newHashMap();
        }
    }

    @Override
    @Transactional
    public void produce(List<SubUser> subUsers) {
        try{
            producerMap.forEach((beanName, producer) -> producer.produce(subUsers));
        } catch (Exception e) {
            log.error("[produce message] -> message produce error, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }
}
