package io.terminus.doctor.event.event;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.search.barn.BarnSearchWriteService;
import io.terminus.doctor.event.search.group.GroupSearchWriteService;
import io.terminus.doctor.event.search.pig.PigSearchWriteService;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

/**
 * Desc: 猪/猪群/猪舍 信息修改刷新ES事件监听
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Component
@Slf4j
public class DoctorZKListener implements EventListener {

    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired
    private PigSearchWriteService pigSearchWriteService;

    @Autowired
    private GroupSearchWriteService groupSearchWriteService;

    @Autowired
    private BarnSearchWriteService barnSearchWriteService;

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
            log.error("ZK subscriber failed, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 处理监听的信息
     */
    @Subscribe
    public void handleEvent(DataEvent dataEvent) {
        log.info("data event data:{}", dataEvent);

        // 1. 如果是猪创建事件信息
        if (DataEventType.PigEventCreate.getKey() == dataEvent.getEventType()) {
            PigEventCreateEvent pigEventCreateEvent = DataEvent.analyseContent(dataEvent, PigEventCreateEvent.class);
            if (pigEventCreateEvent != null && pigEventCreateEvent.getContext() != null) {
                Map<String, Object> context = pigEventCreateEvent.getContext();
                if("single".equals(context.get("contextType"))) {
                    Long pigId = Params.getWithConvert(context, "doctorPigId", d -> Long.valueOf(d.toString()));
                    pigSearchWriteService.update(pigId);
                }else {
                    context.remove("contextType");
                    context.keySet().stream().forEach(s->{
                        pigSearchWriteService.update(Long.valueOf(s));
                    });
                }
            }
        }

        // 2. 如果是猪群信息修改
        if (DataEventType.GroupEventCreate.getKey() == dataEvent.getEventType()) {
            Map<String, Serializable> context = DataEvent.analyseContent(dataEvent, Map.class);
            Long groupId = Params.getWithConvert(context, "doctorGroupId", d -> Long.valueOf(d.toString()));
            // update es index
            groupSearchWriteService.update(groupId);
        }

        // 3. 如果是猪舍信息修改
        if (DataEventType.BarnUpdate.getKey() == dataEvent.getEventType()) {
            Map<String, Serializable> context = DataEvent.analyseContent(dataEvent, Map.class);
            Long barnId = Params.getWithConvert(context, "doctorBarnId", d -> Long.valueOf(d.toString()));
            // update es index
            barnSearchWriteService.update(barnId);
        }
    }
}
