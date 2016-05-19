package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;

import java.util.List;

/**
 * 陈增辉 16/5/19.
 */
public interface DoctorUserService {

    Response<Long> createUserBind(UserBind userBind);
    Response<Long> deleteUserBindById(Long id);
    Response<Long> deleteUserBindByUnique(Long userId, TargetSystem targetSystem);

    Response<UserBind> findUserBindById(Long id);
    Response<List<UserBind>> findUserBindByUserId(Long userId);
    Response<UserBind> findUserBindUnique(Long userId, TargetSystem targetSystem);
}
