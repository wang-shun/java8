package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUserDataPermission;

/**
 * Desc: 用户数据权限写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorUserDataPermissionWriteService {

    /**
     * 创建用户数据权限
     * @param dataPermission 用户数据权限
     * @return 主键id
     */
    Response<Long> createDataPermission(DoctorUserDataPermission dataPermission);

    /**
     * 更新用户数据权限
     * @param dataPermission 用户数据权限对象
     * @return 是否成功
     */
    Response<Boolean> updateDataPermission(DoctorUserDataPermission dataPermission);

    /**
     * 根据主键id删除用户数据权限
     * @param dataPermissionId 主键id
     * @return 是否成功
     */
    Response<Boolean> deleteDataPermission(Long dataPermissionId);

    /**
     * 清除指定用户的缓存
     * @param userId 用户id
     * @return
     */
    Response clearUserCache(Long userId);
}
