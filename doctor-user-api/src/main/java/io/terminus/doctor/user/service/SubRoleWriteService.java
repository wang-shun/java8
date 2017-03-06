package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.SubRole;

/**
 * 子账号自定义角色写服务
 *
 * @author houly
 */
public interface SubRoleWriteService {

    /**
     * 创建自定义角色
     *
     * @param subRole 子账号角色
     * @return 主键 ID
     */
    Response<Long> createRole(SubRole subRole);

    /**
     * 更新自定义角色
     *
     * @param subRole 子账号角色
     * @return 是否更新成功
     */
    Response<Boolean> updateRole(SubRole subRole);

    /**
     * 初始化内置子账号角色权限
     * @param userId 主账号id
     * @param farmId 猪场id
     * @return
     */
    Response<Boolean> initDefaultRoles(String appKey, Long userId, Long farmId);

}
