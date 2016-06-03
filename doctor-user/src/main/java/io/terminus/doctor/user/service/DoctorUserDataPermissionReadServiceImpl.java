package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DoctorUserDataPermissionReadServiceImpl implements DoctorUserDataPermissionReadService{

    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorUserDataPermissionReadServiceImpl(DoctorUserDataPermissionDao doctorUserDataPermissionDao){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Override
    public Response<DoctorUserDataPermission> findDataPermissionByUserId(Long userId) {
        Response<DoctorUserDataPermission> response = new Response<>();
        try {
            response.setResult(doctorUserDataPermissionDao.findByUserId(userId));
        } catch (Exception e) {
            log.error("find DoctorUserDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.user.data.permission");
        }
        return response;
    }

    @Override
    public Response<DoctorUserDataPermission> findDataPermissionById(Long permissionId) {
        Response<DoctorUserDataPermission> response = new Response<>();
        try {
            response.setResult(doctorUserDataPermissionDao.findById(permissionId));
        } catch (Exception e) {
            log.error("find DoctorUserDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.user.data.permission");
        }
        return response;
    }
}
