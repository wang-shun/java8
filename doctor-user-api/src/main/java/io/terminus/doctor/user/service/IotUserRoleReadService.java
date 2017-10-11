package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.IotUserRoleInfo;
import io.terminus.doctor.user.model.IotRole;

import java.util.List;

/**
 * Created by xjn on 17/10/11.
 */
public interface IotUserRoleReadService {

    Response<Paging<IotUserRoleInfo>> paging(String realName, String iotRoleName);

    /**
     * 获取所有有效物联网角色
     * @return 所有物联网角色
     */
    Response<List<IotRole>> listEffected();
}
