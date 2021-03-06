package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * author: 陈增辉
 * Date: 16/5/18
 */
@Slf4j
@Service
@RpcProvider
public class DoctorUserDataPermissionWriteServiceImpl implements DoctorUserDataPermissionWriteService{
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorUserDataPermissionWriteServiceImpl(DoctorUserDataPermissionDao doctorUserDataPermissionDao){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Override
    public Response<Long> createDataPermission(DoctorUserDataPermission dataPermission) {
        Response<Long> response = new Response<>();
        try {
            doctorUserDataPermissionDao.create(dataPermission);
            response.setResult(dataPermission.getId());
        } catch (Exception e) {
            log.error("createDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("create.data.permission.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateDataPermission(DoctorUserDataPermission dataPermission) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorUserDataPermissionDao.update(dataPermission));
        } catch (Exception e) {
            log.error("updateDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.data.permission.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteDataPermission(Long dataPermissionId) {
        Response<Boolean> response = new Response<>();
        try {
            DoctorUserDataPermission permission = doctorUserDataPermissionDao.findById(dataPermissionId);
            if(permission != null){
                doctorUserDataPermissionDao.delete(dataPermissionId);
            }
            response.setResult(true);
        } catch (Exception e) {
            log.error("deleteDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.data.permission.failed");
        }
        return response;
    }
}
