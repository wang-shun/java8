package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.parana.user.model.User;

/**
 * 陈增辉 16/5/16.
 * 账户服务
 */
public interface AccountService {

    /**
     * 查询用户在其他系统绑定的账号
     * @param userId
     * @param targetSystem 目标系统,  关联枚举: @see io.terminus.doctor.user.enums.TargetSystem
     * @return 目标系统的User对象, 有效字段:id, name, mobile, email, type, status
     */
    Response<User> findBindAccount(Long userId, Integer targetSystem);

    /**
     * 绑定用户指定的目标系统的账号
     * @param userId
     * @param targetSystem 目标系统,  关联枚举: @see io.terminus.doctor.user.enums.TargetSystem
     * @param account 用户填写的目标系统的账号
     * @param password 用户填写的目标系统的明文密码,不加密
     * @return 目标系统的User对象
     */
    Response<User> bindAccount(Long userId, Integer targetSystem, String account, String password);

    /**
     * 解除用户在目标系统的账号绑定
     * @param userId
     * @param targetSystem 目标系统,  关联枚举: @see io.terminus.doctor.user.enums.TargetSystem
     * @return 解除绑定之前的目标系统的User对象
     */
    Response<User> unbindAccount(Long userId, Integer targetSystem);

}
