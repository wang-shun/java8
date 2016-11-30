package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.search.barn.BarnSearchWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 16/11/9.
 * 事件监听
 */
@Slf4j
@Component
public class DoctorBarnEventListener implements EventListener{

    @Autowired
    private BarnSearchWriteService barnSearchWriteService;

    /**
     * 监听处理有关猪舍的事件
     * @param event
     */
    @Subscribe
    public void DoctorBarnEventListener(ListenedBarnEvent event){
        log.info("[DoctorBarnEventListener] -> handle.barn.event, event -> {}", event);
        RespHelper.orServEx(barnSearchWriteService.update(event.getBarnId()));
    }
}
