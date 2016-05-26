package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupEvent;

/**
 * Desc: 猪群事件表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupEventWriteService {

    /**
     * 创建DoctorGroupEvent
     * @param groupEvent
     * @return 主键id
     */
    Response<Long> createGroupEvent(DoctorGroupEvent groupEvent);

    /**
     * 更新DoctorGroupEvent
     * @param groupEvent
     * @return 是否成功
     */
    Response<Boolean> updateGroupEvent(DoctorGroupEvent groupEvent);

    /**
     * 根据主键id删除DoctorGroupEvent
     * @param groupEventId
     * @return 是否成功
     */
    Response<Boolean> deleteGroupEventById(Long groupEventId);
}