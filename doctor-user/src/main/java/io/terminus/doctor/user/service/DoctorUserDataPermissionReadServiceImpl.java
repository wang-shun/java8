package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.cache.CacheCenter;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * author: 陈增辉
 * Date: 16/5/18
 */
@Slf4j
@Service
@RpcProvider
public class DoctorUserDataPermissionReadServiceImpl implements DoctorUserDataPermissionReadService{

    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final CacheCenter cacheCenter;

    @Autowired
    public DoctorUserDataPermissionReadServiceImpl(DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                                   CacheCenter cacheCenter){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.cacheCenter = cacheCenter;
    }

    @Override
    public Response<DoctorUserDataPermission> findDataPermissionByUserId(Long userId) {
        Response<DoctorUserDataPermission> response = new Response<>();
        try {
            response.setResult(cacheCenter.getUserDataPermission(userId));
        } catch (Exception e) {
            log.error("find DoctorUserDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.user.data.permission");
        }
        return response;
    }

    @Override
    public Response<List<DoctorUserDataPermission>> findDataPermissionByUserIds(List<Long> userIds){
        Response<List<DoctorUserDataPermission>> response = new Response<>();
        try {
            response.setResult(doctorUserDataPermissionDao.findByUserIds(userIds));
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
