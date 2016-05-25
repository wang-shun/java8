package io.terminus.doctor.user.interfaces.service;

import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;

import java.util.List;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserWriteInterface {

    Response<Integer> updateStatus(Long userId, Integer status);
    Response<Integer> batchUpdateStatus(List<Long> userIds, Integer status);

    Response<Integer> updateType(Long userId, int type);
    Response<Integer> batchUpdateType(List<Long> userIds, int type);

    Response<Boolean> update(User user);

    Response<Boolean> createUser(User user);
    Response<Integer> createUsers(List<User> users);

    Response<Boolean> delete(Long id);
    Response<Integer> deletes(List<Long> ids);
    Response<Integer> deletes(Long id0, Long id1, Long... idn);
}
