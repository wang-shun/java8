package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
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
@Primary
public class MockDoctorUserDataPermissionWriteServiceImpl implements DoctorUserDataPermissionWriteService {
    @Override
    public Response<Long> createDataPermission(DoctorUserDataPermission dataPermission) {
        return Response.ok(1L);
    }

    @Override
    public Response<Boolean> updateDataPermission(DoctorUserDataPermission dataPermission) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> deleteDataPermission(Long dataPermissionId) {
        return Response.ok(Boolean.TRUE);
    }
}
