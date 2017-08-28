package io.terminus.doctor.move.manager;

import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilder;
import io.terminus.doctor.move.builder.group.DoctorGroupEventInputBuilders;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.builder.pig.DoctorPigEventInputBuilders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/9.
 * 获取数据输入的构建器
 */
@Slf4j
@Component
public class DoctorBuilderFactory {

    @Autowired
    private DoctorPigEventInputBuilders doctorPigEventInputBuilders;
    @Autowired
    private DoctorGroupEventInputBuilders doctorGroupEventInputBuilders;

    public DoctorPigEventInputBuilder getPigBuilder(String eventName) {
        return doctorPigEventInputBuilders.getPigEvenInputBuilderMap().get(eventName);
//        return checkNotNull(doctorPigEventInputBuilders.getPigEvenInputBuilderMap().get(eventName),
//                "eventName:" + eventName);
    }

    public DoctorGroupEventInputBuilder getGroupBuilder(String eventName) {
        return doctorGroupEventInputBuilders.getGroupEventInputBuilderMap().get(eventName);
    }
}
