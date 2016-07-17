package io.terminus.doctor.basic.event;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.basic.search.material.MaterialSearchWriteService;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.Params;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 物料信息修改事件监听
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Component
@Slf4j
public class DoctorMaterialZKLinstener implements EventListener {

    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired
    private MaterialSearchWriteService materialSearchWriteService;

    @PostConstruct
    public void subs() {
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
            log.error("Doctor material ZK subscribe failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 物料信息修改事件监听
     */
    @Subscribe
    public void handleEvent(DataEvent dataEvent) {
        log.info("data event data:{}", dataEvent);

        // 物料信息创建事件信息
        if (Objects.equals(DataEventType.MaterialInfoCreateEvent.getKey(), dataEvent.getEventType())) {
            Map<String, Serializable> map = DataEvent.analyseContent(dataEvent, Map.class);
            Long materialId = Params.getWithConvert(map, "materialInfoCreatedId", a -> Long.valueOf(a.toString()));
            materialSearchWriteService.update(materialId);
        }
    }
}
