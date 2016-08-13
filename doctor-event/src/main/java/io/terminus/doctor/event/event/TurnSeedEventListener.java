package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 转转种猪事件监听器
 */

@Slf4j
@Component
public class TurnSeedEventListener implements EventListener{

    @Autowired(required = false)
    private Publisher publisher;
    private final CoreEventDispatcher coreEventDispatcher;

    @Autowired
    public TurnSeedEventListener(CoreEventDispatcher coreEventDispatcher){
        this.coreEventDispatcher = coreEventDispatcher;
    }

    @Subscribe
    public void turnSeedEvent(TurnSeedEvent event){
        if(publisher == null){
            coreEventDispatcher.publish(DataEvent.make(DataEventType.PigEventCreate.getKey(), new PigEventCreateEvent(event.getCreateCasualPigEventResult())));
        }else{
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new PigEventCreateEvent(event.getCreateCasualPigEventResult())));
            }catch (Exception e){
                log.error("failed to publish event, cause:{}", e);
            }
        }
        coreEventDispatcher.publish(DoctorPigCountEvent.builder()
                .farmId(event.getBasicInputInfoDto().getFarmId())
                .orgId(event.getBasicInputInfoDto().getOrgId())
                .pigType(event.getBasicInputInfoDto().getPigType()).build());
    }
}
