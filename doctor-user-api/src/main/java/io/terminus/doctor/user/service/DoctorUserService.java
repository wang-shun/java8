package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;

import java.util.List;

/**
 * 陈增辉 16/5/19.
 */
public interface DoctorUserService {

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
    Response<Boolean> deleteUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem);

    /**
     * 查询用户绑定关系
     * @param id
     * @return
     */
    Response<UserBind> findUserBindById(Long id);
    Response<List<UserBind>> findUserBindByUserId(Long userId);
    Response<UserBind> findUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem);
}
