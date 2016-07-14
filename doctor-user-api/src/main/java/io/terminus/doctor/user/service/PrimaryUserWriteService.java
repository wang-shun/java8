package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;

/**
 * 主账号写服务
 *
 * @author Effet
 */
public interface PrimaryUserWriteService {

    /**
     * 创建卖家
     *
     * @param primaryUser 主账号信息
     * @return 卖家表主键 ID
     */
    Response<Long> createPrimaryUser(PrimaryUser primaryUser);

    /**
     * 更新卖家
     *
     * @param primaryUser 主账号信息
     * @return 是否更新
     */
    Response<Boolean> updatePrimaryUser(PrimaryUser primaryUser);

    /**
     * 创建主账号的子账户
     *
     * @param sub 子账户关联信息
     * @return 关键表主键 ID
     */
    Response<Long> createSub(Sub sub);

    /**
     * 更新主账号的子账户
     *
     * @param sub 子账户关联信息
     * @return 是否更新
     */
    Response<Boolean> updateSub(Sub sub);

    /**
     * 子账号角色名称更新后,表 doctor_user_subs 中的冗余字段也需要跟着更新
     * @param subRoleId 表 doctor_sub_roles 的 主键id, 关联表 doctor_user_subs 的 role_id
     * @param newRoleName 新的角色名称
     * @return
     */
    Response<Boolean> updateRoleName(Long subRoleId, String newRoleName);
}
