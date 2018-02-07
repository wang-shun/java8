package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.IotUserDto;
import io.terminus.doctor.user.model.IotRole;

/**
 * Created by xjn on 17/10/11.
 */
public interface IotUserRoleWriteService {

    /**
     * 创建物联网角色
     * @param iotRole 物联网角色
     * @return 是否成功
     */
    Response<Boolean> createIotRole(IotRole iotRole);

    /**
     * 更新物联网角色
     * @param iotRole 物联网角色
     * @return 是否成功
     */
    Response<Boolean> updateIotRole(IotRole iotRole);

    /**
     * 创建或更新物联网运营用户"
     * @param iotUserDto 物联网运营用户"
     * @return 是否成功
     */
    Response<Boolean> createIotUser(IotUserDto iotUserDto);

    /**
     * 更新用户与物联网角色的关联
     * @param iotUserDto 关联
     * @return 是否成功
     */
    Response<Boolean> updateIotUser(IotUserDto iotUserDto);
}
