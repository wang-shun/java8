package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;

import java.util.List;

/**
 * 陈增辉 16/5/19.
 * 与用户相关的业务service
 */
public interface UserBindReadService {

    /**
     * 查询用户绑定关系
     * @param id
     * @return
     */
    Response<UserBind> findUserBindById(Long id);

    /**
     * 查询用户绑定关系
     * @param userId
     * @return
     */
    Response<List<UserBind>> findUserBindByUserId(Long userId);

    /**
     * 查询用户绑定关系
     * @param userId
     * @param targetSystem
     * @return
     */
    Response<UserBind> findUserBindByUserIdAndTargetSystem(Long userId, TargetSystem targetSystem);
}
