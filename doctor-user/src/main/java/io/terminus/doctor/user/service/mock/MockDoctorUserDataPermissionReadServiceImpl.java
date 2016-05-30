package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class MockDoctorUserDataPermissionReadServiceImpl implements DoctorUserDataPermissionReadService {
    @Override
    public Response<DoctorUserDataPermission> findDataPermissionByUserId(Long userId) {
        return Response.ok(mockPermission(userId, userId));
    }

    @Override
    public Response<DoctorUserDataPermission> findDataPermissionById(Long permissionId) {
        return Response.ok(mockPermission(permissionId, permissionId));
    }

    private DoctorUserDataPermission mockPermission(Long userId, Long permissionId) {
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setId(permissionId);
        permission.setUserId(userId);
        permission.setFarmIds("1,2,3,4");
        return permission;
    }
}
