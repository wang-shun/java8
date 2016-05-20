package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorUnit;

/**
 * Desc: 计量单位表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorUnitWriteService {

    /**
     * 创建DoctorUnit
     * @param unit
     * @return 主键id
     */
    Response<Long> createUnit(DoctorUnit unit);

    /**
     * 更新DoctorUnit
     * @param unit
     * @return 是否成功
     */
    Response<Boolean> updateUnit(DoctorUnit unit);

    /**
     * 根据主键id删除DoctorUnit
     * @param unitId
     * @return 是否成功
     */
    Response<Boolean> deleteUnitById(Long unitId);
}