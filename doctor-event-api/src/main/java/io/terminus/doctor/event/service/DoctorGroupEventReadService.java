package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupEvent;

import java.util.List;

/**
 * Desc: 猪群事件表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupEventReadService {

    /**
     * 根据id查询猪群事件表
     * @param groupEventId 主键id
     * @return 猪群事件表
     */
    Response<DoctorGroupEvent> findGroupEventById(Long groupEventId);

    /**
     * 根据farmId查询猪群事件表
     * @param farmId 猪场id
     * @return 猪群事件表
     */
    Response<List<DoctorGroupEvent>> findGroupEventsByFarmId(Long farmId);
}
