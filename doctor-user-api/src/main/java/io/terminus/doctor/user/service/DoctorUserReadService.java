package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorStaff;

import javax.validation.constraints.NotNull;

/**
 * Desc: 用户读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorUserReadService {

    /**
     * 根据用户id查询用户前台角色类型
     * @param userId 用户id
     * @return 角色类型
     * @see io.terminus.doctor.user.enums.RoleType
     */
    Response<Integer> findUserRoleTypeByUserId(Long userId);

    /**
     * 查询用户基本信息
     * @param userId 用户id
     * @return 用户信息
     */
    Response<DoctorUserInfoDto> findUserInfoByUserId(Long userId);

    /**
     * 查询猪场职员基本信息
     * @param userId 用户id
     * @return 猪场职员信息
     */
    Response<DoctorStaff> findStaffByUserId(@NotNull(message = "userId.not.null") Long userId);
}
