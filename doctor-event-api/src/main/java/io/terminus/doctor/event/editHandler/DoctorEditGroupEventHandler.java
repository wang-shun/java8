package io.terminus.doctor.event.editHandler;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:07 17/3/8
 */

public interface DoctorEditGroupEventHandler {

    /**
     * 根据doctorGroupTrack doctorGroupEvent 推导出下个doctorGroupTrack
     * @param doctorGroupTrack
     * @param doctorGroupEvent
     * @return
     */
    DoctorGroupTrack handle(List<DoctorGroupEvent> doctorGroupEventList, DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent);
}
