package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorChangeReason;

/**
 * Desc: 变动类型表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorChangeReasonWriteService {

    /**
     * 创建DoctorChangeReason
     * @param changeReason
     * @return 主键id
     */
    Response<Long> createChangeReason(DoctorChangeReason changeReason);

    /**
     * 更新DoctorChangeReason
     * @param changeReason
     * @return 是否成功
     */
    Response<Boolean> updateChangeReason(DoctorChangeReason changeReason);

    /**
     * 根据主键id删除DoctorChangeReason
     * @param changeReasonId
     * @return 是否成功
     */
    Response<Boolean> deleteChangeReasonById(Long changeReasonId);
}