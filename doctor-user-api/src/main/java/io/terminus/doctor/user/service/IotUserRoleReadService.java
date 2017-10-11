package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.IotUserRoleInfo;

/**
 * Created by xjn on 17/10/11.
 */
public interface IotUserRoleReadService {

    Response<Paging<IotUserRoleInfo>> paging(String realName, String iotRoleName);
}
