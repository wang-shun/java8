package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorRoleContent;

/**
 * Created by yudi on 2016/12/7.
 * Mail to yd@terminus.io
 */
public interface DoctorUserRoleLoader {
    Response<DoctorRoleContent> hardLoadRoles(Long userId);
}
