package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorUnit;

/**
 * Desc: 计量单位表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorUnitReadService {

    /**
     * 根据id查询计量单位表
     * @param unitId 主键id
     * @return 计量单位表
     */
    Response<DoctorUnit> findUnitById(Long unitId);

}
