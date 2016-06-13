package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Desc:
 * author: 陈增辉
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

    //cache, key = userId, value = permission
    private LoadingCache<Long, DoctorUserDataPermission> permissionLoadingCache;

    @PostConstruct
    public void initCache(){
        permissionLoadingCache = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build(new CacheLoader<Long, DoctorUserDataPermission>() {
            @Override
            public DoctorUserDataPermission load(Long userId) throws Exception {
                return doctorUserDataPermissionDao.findByUserId(userId);
            }
        });
    }

    @Override
    public Response<DoctorUserDataPermission> findDataPermissionByUserId(Long userId) {
        Response<DoctorUserDataPermission> response = new Response<>();
        try {
            response.setResult(permissionLoadingCache.getUnchecked(userId));
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
