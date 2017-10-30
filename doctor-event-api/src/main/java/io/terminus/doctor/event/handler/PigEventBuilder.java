package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
public interface PigEventBuilder {


    boolean isSupportedEvent(DoctorPigEvent pigEvent);

    void buildEvent(String eventDto, DoctorPigEvent pigEvent);

}
