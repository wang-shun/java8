package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorRevertLog;

/**
 * Desc: 回滚记录表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorRevertLogWriteService {

    /**
     * 创建DoctorRevertLog
     * @param revertLog
     * @return 主键id
     */
    Response<Long> createRevertLog(DoctorRevertLog revertLog);

    /**
     * 更新DoctorRevertLog
     * @param revertLog
     * @return 是否成功
     */
    Response<Boolean> updateRevertLog(DoctorRevertLog revertLog);

    /**
     * 根据主键id删除DoctorRevertLog
     * @param revertLogId
     * @return 是否成功
     */
    Response<Boolean> deleteRevertLogById(Long revertLogId);
}