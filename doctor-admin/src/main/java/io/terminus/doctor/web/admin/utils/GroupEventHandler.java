package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.model.DoctorGroupEvent;


/**
 * Created by sunbo@terminus.io on 2017/9/15.
 */
public interface GroupEventHandler {

    boolean isSupported(DoctorGroupEvent groupEvent);


    void updateEvent(String eventDto, DoctorGroupEvent groupEvent);
}
