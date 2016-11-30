package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorRollbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/30
 */
@Slf4j
@Component
public class DoctorRollbackEventListener implements EventListener {

    @Autowired
    private DoctorRollbackService doctorRollbackService;

    /**
     * 监听处理回滚事件
     * @param event 回滚事件携带信息
     */
    @Subscribe
    public void handleRollbackEvent(ListenedRollbackEvent event){
        log.info("[DoctorRollbackEventListener] -> handle.rollback.event, event -> {}", event);
        RespHelper.orServEx(doctorRollbackService.rollbackReportAndES(event.getDoctorRollbackDtos()));
    }

}
