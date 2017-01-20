package io.terminus.doctor.web.core.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.web.core.dto.DoctorBasicDto;
import io.terminus.doctor.web.core.dto.DoctorFarmBasicDto;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪只统计读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */

public interface DoctorStatisticReadService {

    /**
     * 获取单个猪场统计信息
     * @param farmId  猪场id
     * @return 猪场信息
     */
    Response<DoctorFarmBasicDto> getFarmStatistic(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @param userId  用户id
     * @return 猪场信息list
     */
    Response<DoctorBasicDto> getOrgStatistic(@NotNull(message = "userId.not.null") Long userId);
    Response<DoctorBasicDto> getOrgStatisticByOrg(@NotNull(message = "userId.not.null") Long userId, @NotNull(message = "orgId.not.null")Long orgId);
}
