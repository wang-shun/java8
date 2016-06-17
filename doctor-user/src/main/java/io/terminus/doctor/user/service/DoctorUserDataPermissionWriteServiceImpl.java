package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.event.CacheEvent;
import io.terminus.doctor.common.event.CacheMessage;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.event.UserDataPermissionModifyEvent;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.zookeeper.pubsub.Publisher;
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
public class DoctorUserDataPermissionWriteServiceImpl implements DoctorUserDataPermissionWriteService{
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorUserDataPermissionWriteServiceImpl(DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                                    CoreEventDispatcher coreEventDispatcher){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.coreEventDispatcher = coreEventDispatcher;
    }

    @Override
    public Response<Long> createDataPermission(DoctorUserDataPermission dataPermission) {
        Response<Long> response = new Response<>();
        try {
            doctorUserDataPermissionDao.create(dataPermission);
            this.publish(dataPermission.getUserId());
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
            this.publish(dataPermission.getUserId());
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
                this.publish(permission.getUserId());
                doctorUserDataPermissionDao.delete(dataPermissionId);
            }
            response.setResult(true);
        } catch (Exception e) {
            log.error("deleteDataPermission failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.data.permission.failed");
        }
        return response;
    }

    /**
     * 当用户权限更新或删除时分发一个事件,监听器将会使缓存中的相应数据失效
     * @param userId
     */
    private void publish(Long userId) {
        if (publisher != null){
            try {
                publisher.publish(CacheEvent.toBytes(CacheEvent.make(CacheMessage.USER_DATA_PERMISSION.getValue(), userId)));
            } catch (Exception e) {
                log.error("failed to publish cache event, cause: {}", Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(new UserDataPermissionModifyEvent(userId));
        }
    }
}
