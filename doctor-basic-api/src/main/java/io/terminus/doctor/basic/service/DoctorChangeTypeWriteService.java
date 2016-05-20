package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorChangeType;

/**
 * Desc: 变动类型表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorChangeTypeWriteService {

    /**
     * 创建DoctorChangeType
     * @param changeType
     * @return 主键id
     */
    Response<Long> createChangeType(DoctorChangeType changeType);

    /**
     * 更新DoctorChangeType
     * @param changeType
     * @return 是否成功
     */
    Response<Boolean> updateChangeType(DoctorChangeType changeType);

    /**
     * 根据主键id删除DoctorChangeType
     * @param changeTypeId
     * @return 是否成功
     */
    Response<Boolean> deleteChangeTypeById(Long changeTypeId);
}