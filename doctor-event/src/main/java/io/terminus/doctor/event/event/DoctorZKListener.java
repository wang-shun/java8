package io.terminus.doctor.event.event;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.search.pig.PigSearchWriteService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 猪/猪群/猪舍 信息修改刷新ES事件监听
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Component
@Slf4j
public class DoctorZKListener {

    @Autowired
    private Subscriber subscriber;

    @Autowired
    private PigSearchWriteService pigSearchWriteService;

    @PostConstruct
    public void subs() {
        try{
            subscriber.subscribe(data -> {
                DataEvent dataEvent = DataEvent.fromBytes(data);
                if (dataEvent != null && dataEvent.getEventType() != null) {
                    handleEvent(dataEvent);
                }
            });
        } catch (Exception e) {
            log.error("ZK subscriber failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 处理监听的信息
     */
    private void handleEvent(DataEvent dataEvent) {

        // 1. 如果是猪创建事件信息
        if (Objects.equals(DataEventType.PigEventCreate.getKey(), dataEvent.getEventType())) {
            PigEventCreateEvent pigEventCreateEvent = DataEvent.analyseContent(dataEvent, PigEventCreateEvent.class);
            if (pigEventCreateEvent != null && pigEventCreateEvent.getContext() != null) {
                Map<String, Object> context = pigEventCreateEvent.getContext();
                Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
                // update es index
                pigSearchWriteService.update(pigId);
            }
        }

        // 2. 如果是猪群信息修改
        // TODO : ZK
    }
}
