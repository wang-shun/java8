package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorBarn;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    Response<Long> createBarn(@Valid DoctorBarn barn);

    /**
     * 更新DoctorBarn
     * @param barn
     * @return 是否成功
     */
    Response<Boolean> updateBarn(@Valid DoctorBarn barn);

    /**
     * 根据主键id删除DoctorBarn
     * @param barnId
     * @return 是否成功
     */
    Response<Boolean> deleteBarnById(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 修改猪舍状态
     * @param barnId 猪舍id
     * @param status 猪舍状态
     * @return 是否成功
     */
    Response<Boolean> updateBarnStatus(@NotNull(message = "barnId.not.null") Long barnId,
                                    @NotNull(message = "status.not.null") Integer status);

    /**
     * 发猪舍变动事件
     * @param barnId 猪舍id
     * @return 是否成功
     */
    Response<Boolean> publistBarnEvent(@NotNull(message = "barnId.not.null") Long barnId);
}