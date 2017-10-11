package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUserRole;

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
     * 创建用户与物联网角色的关联
     * @param iotUserRole 关联
     * @return 是否成功
     */
    Response<Boolean> createIotUserRole(IotUserRole iotUserRole);
}
