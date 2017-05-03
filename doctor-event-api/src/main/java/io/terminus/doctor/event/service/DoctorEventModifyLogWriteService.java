package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorEventModifyLog;

/**
 * Code generated by terminus code gen
 * Desc: 写服务
 * Date: 2017-04-05
 */

public interface DoctorEventModifyLogWriteService {

    /**
     * 创建DoctorEventModifyLog
     * @param doctorEventModifyLog
     * @return 主键id
     */
    Response<Long> createDoctorEventModifyLog(DoctorEventModifyLog doctorEventModifyLog);

    /**
     * 更新DoctorEventModifyLog
     * @param doctorEventModifyLog
     * @return 是否成功
     */
    Response<Boolean> updateDoctorEventModifyLog(DoctorEventModifyLog doctorEventModifyLog);

    /**
     * 根据主键id删除DoctorEventModifyLog
     * @param doctorEventModifyLogId
     * @return 是否成功
     */
    Response<Boolean> deleteDoctorEventModifyLogById(Long doctorEventModifyLogId);
}