package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.enums.EventElicitStatus;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:17 17/3/8
 */

public interface DoctorEditGroupEventService {

    Response<String> elicitDoctorGroupTrack(DoctorGroupEvent doctorGroupEvent);
}
