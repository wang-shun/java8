package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUserDataPermission;

import java.util.List;

/**
 * Desc: 用户数据权限读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorUserDataPermissionReadService {

    /**
     * 根据用户id查询所属数据权限
     * @param userId  用户id
     * @return 数据权限
     */
    Response<DoctorUserDataPermission> findDataPermissionByUserId(Long userId);

    /**
     * 根据用户id 批量查询所属数据权限
     * @param userIds  用户id
     * @return 数据权限
     */
    Response<List<DoctorUserDataPermission>> findDataPermissionByUserIds(List<Long> userIds);

    /**
     * 根据id查询所属数据权限
     * @param permissionId  id
     * @return 数据权限
     */
    Response<DoctorUserDataPermission> findDataPermissionById(Long permissionId);
}
