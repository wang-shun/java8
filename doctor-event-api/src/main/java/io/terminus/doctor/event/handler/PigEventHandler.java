package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
public interface PigEventHandler {


    boolean isSupportedEvent(DoctorPigEvent pigEvent);

    void updateEvent(String eventDto, DoctorPigEvent pigEvent);

    void changePig(DoctorPigEvent pigEvent);

}
