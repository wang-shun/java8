package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorRevertLog;

/**
 * Desc: 回滚记录表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorRevertLogReadService {

    /**
     * 根据id查询回滚记录表
     * @param revertLogId 主键id
     * @return 回滚记录表
     */
    Response<DoctorRevertLog> findRevertLogById(Long revertLogId);

}
