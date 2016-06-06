package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;

import java.util.List;

/**
 * 陈增辉 16/5/19.
 * 与用户相关的业务service
 */
public interface UserBindWriteService {

    /**
     * 创建本系统用户与其他系统账户的绑定关系
     * @param userBind
     * @return
     */
    Response<Boolean> createUserBind(UserBind userBind);

    /**
     * 删除用户绑定关系
     * @param id 表user_bind中的主键
     * @return
     */
    Response<Boolean> deleteUserBindById(Long id);

    /**
     *  删除用户绑定关系
     * @param userId
     * @param targetSystem
     * @return
     */
    Response<Boolean> deleteUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem);

}
