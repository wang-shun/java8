package io.terminus.doctor.basic.event;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.basic.cache.DoctorBasicCacher;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/27
 */
@Slf4j
@Component
public class DoctorBasicEventListener implements EventListener {

    @Autowired
    private DoctorBasicCacher doctorBasicCacher;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired(required = false)
    private Subscriber subscriber;

    @PostConstruct
    public void fire() {
        try{
            if (subscriber == null) {
                return;
            }
            subscriber.subscribe(data -> {
                DataEvent dataEvent = DataEvent.fromBytes(data);
                if (dataEvent != null && dataEvent.getEventType() != null) {
                    coreEventDispatcher.publish(dataEvent);
                }
            });
        } catch (Exception e) {
            log.error("doctor basic subscriber init failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 清理基础数据缓存
     */
    @Subscribe
    public void refreshBasicCache(DataEvent dataEvent) {
        if (Objects.equals(DataEventType.BasicUpdate.getKey(), dataEvent.getEventType())) {
            log.info("data event data:{}", dataEvent);
            try {
                doctorBasicCacher.refresh(DataEvent.analyseContent(dataEvent, DoctorBasic.class).getType());
            } catch (Exception e) {
                doctorBasicCacher.clear();
            }
        }
    }
}
