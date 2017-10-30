package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by sunbo@terminus.io on 2017/10/9.
 */
public interface PigEventHandler {

    boolean isSupportedEvent(DoctorPigEvent pigEvent);

    void handle(DoctorPigEvent pigEvent);
}
