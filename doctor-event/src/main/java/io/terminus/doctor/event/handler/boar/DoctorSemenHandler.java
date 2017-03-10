package io.terminus.doctor.event.handler.boar;

import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorSemenHandler extends DoctorAbstractEventHandler{
    @Override
    protected void createPigSnapshot(DoctorPigTrack toTrack, DoctorPigEvent executeEvent, Long lastEventId) {
    }
}
