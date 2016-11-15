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
     * @param user 用户信息
     * @param systemCode 系统标识
     * @return 是否成功
     */
    RespDto<Boolean> update(UserDto user, String systemCode);

    /**
     * 创建用户
     * @param user 用户信息
     * @param systemCode 系统标识
     * @return 创建的用户
     */
    RespDto<UserDto> createUser(UserDto user, String systemCode);

    /**
     * 删除用户
     * @param id 用户id
     * @return 是否成功
     */
    RespDto<Boolean> delete(Long id, String systemCode);

    /**
     * 批量删除用户
     * @param ids 用户id
     * @return
     */
    RespDto<Integer> deletes(List<Long> ids, String systemCode);
}
