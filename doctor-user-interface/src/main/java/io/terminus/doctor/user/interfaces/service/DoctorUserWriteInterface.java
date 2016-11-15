package io.terminus.doctor.user.interfaces.service;

import io.terminus.doctor.user.interfaces.model.RespDto;
import io.terminus.doctor.user.interfaces.model.UserDto;

import java.util.List;

/**
 * 陈增辉 16/5/23.
 * 对外提供与用户相关的dubbo接口
 */
public interface DoctorUserWriteInterface {

    /**
     * 更新用户基本信息, 仅支持更新 name, email, mobile, password
     * @param user
     * @return
     */
    RespDto<Boolean> update(UserDto user);

    /**
     * 创建用户
     * @param user
     * @return
     */
    RespDto<UserDto> createUser(UserDto user);

    /**
     * 删除用户
     * @param id 用户id
     * @return
     */
    RespDto<Boolean> delete(Long id);

    /**
     * 批量删除用户
     * @param ids 用户id
     * @return
     */
    RespDto<Integer> deletes(List<Long> ids);

    /**
     * 批量删除用户
     * @param id0 用户id
     * @param id1 用户id
     * @param idn 用户id
     * @return
     */
    RespDto<Integer> deletes(Long id0, Long id1, Long... idn);

}
