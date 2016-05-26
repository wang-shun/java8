package io.terminus.doctor.user.interfaces.service;

import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;

import java.util.List;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserWriteInterface {

    /**
     * 更新用户状态
     * @param userId
     * @param status 0:未激活, 1:正常, -1:锁定, -2:冻结, -3: 删除
     * @return
     */
    Response<Integer> updateStatus(Long userId, Integer status);

    /**
     * 批量更新用户状态
     * @param userIds
     * @param status 0:未激活, 1:正常, -1:锁定, -2:冻结, -3: 删除
     * @return
     */
    Response<Integer> batchUpdateStatus(List<Long> userIds, Integer status);

    /**
     * 更新用户类型
     * @param userId
     * @param typeName 可选: ADMIN, SELLER, BUYER, SITE_OWNER, AGENT, OPERATOR
     * @return
     */
    Response<Integer> updateType(Long userId, String typeName);

    /**
     * 更新用户基本信息, 仅支持更新 name, email, mobile, password, roles_json
     * @param user
     * @return
     */
    Response<Boolean> update(User user);

    /**
     * 创建用户
     * @param user
     * @return
     */
    Response<User> createUser(User user);

    /**
     * 删除用户
     * @param id 用户id
     * @return
     */
    Response<Boolean> delete(Long id);

    /**
     * 批量删除用户
     * @param ids 用户id
     * @return
     */
    Response<Integer> deletes(List<Long> ids);

    /**
     * 批量删除用户
     * @param id0 用户id
     * @param id1 用户id
     * @param idn 用户id
     * @return
     */
    Response<Integer> deletes(Long id0, Long id1, Long... idn);
}
