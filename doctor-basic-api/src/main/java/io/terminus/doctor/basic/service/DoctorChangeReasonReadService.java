package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorChangeReason;

/**
 * Desc: 变动类型表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorChangeReasonReadService {

    /**
     * 根据id查询变动类型表
     * @param changeReasonId 主键id
     * @return 变动类型表
     */
    Response<DoctorChangeReason> findChangeReasonById(Long changeReasonId);

}
