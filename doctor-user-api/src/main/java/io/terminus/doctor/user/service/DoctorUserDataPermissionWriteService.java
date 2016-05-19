package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUserDataPermission;

/**
 * Desc: 用户数据权限写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorUserDataPermissionWriteService {

    Response<Long> createDataPermission(DoctorUserDataPermission dataPermission);

    Response<Boolean> updateDataPermission(DoctorUserDataPermission dataPermission);

    Response<Boolean> deleteDataPermission(Long dataPermissionId);
}
