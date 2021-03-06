package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.model.DoctorGroupEvent;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:17 17/3/8
 */

public interface DoctorEditGroupEventService {

    Response<Boolean> reElicitGroupEvent(List<Long> groupIds);

    List<DoctorEventInfo> elicitDoctorGroupTrackRebuildOne(DoctorGroupEvent doctorGroupEvent, Long modifyRequestId);
}
