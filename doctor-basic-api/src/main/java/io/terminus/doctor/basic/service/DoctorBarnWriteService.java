package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBarn;

/**
 * Desc: 猪舍表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBarnWriteService {

    /**
     * 创建DoctorBarn
     * @param barn
     * @return 主键id
     */
    Response<Long> createBarn(DoctorBarn barn);

    /**
     * 更新DoctorBarn
     * @param barn
     * @return 是否成功
     */
    Response<Boolean> updateBarn(DoctorBarn barn);

    /**
     * 根据主键id删除DoctorBarn
     * @param barnId
     * @return 是否成功
     */
    Response<Boolean> deleteBarnById(Long barnId);
}