package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/28
 */
@Slf4j
@Component
public class DoctorPigEventListener implements EventListener {

    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;

    @Subscribe
    public void handlePigEvent(ListenedPigEvent listenedPigEvent) {
        DoctorPigEvent event = RespHelper.orServEx(doctorPigEventReadService.queryPigEventById(listenedPigEvent.getPigEventId()));
        PigEvent eventType = PigEvent.from(event.getType());
        if (eventType == null) {
            log.error("handle pig event type not find, listenPigEvent:{}, event:{}", listenedPigEvent, event);
            return;
        }

        switch (eventType) {
            case CHG_FARM:

                break;
            case REMOVAL:

                break;
            case ENTRY:

                break;
            case MATING:

                break;
            case PREG_CHECK:

                break;
            case FARROWING:

                break;
            case WEAN:

                break;
            default:

                break;
        }
    }


}
