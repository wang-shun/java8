package io.terminus.doctor.user.cache;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.common.event.CacheEvent;
import io.terminus.doctor.common.event.CacheMessage;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.event.UserDataPermissionModifyEvent;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.zookeeper.pubsub.SubscribeCallback;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 陈增辉 on 16/6/14.
 */
@Component
@Slf4j
public class CacheCenter {

    @Autowired(required = false)
    private Subscriber cacheListener;
    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public CacheCenter(DoctorUserDataPermissionDao doctorUserDataPermissionDao){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    //cache, key = userId, value = permission
    private LoadingCache<Long, DoctorUserDataPermission> permissionLoadingCache;

    @PostConstruct
    public void initCache(){
        permissionLoadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, DoctorUserDataPermission>() {
            @Override
            public DoctorUserDataPermission load(Long userId) throws Exception {
                return doctorUserDataPermissionDao.findByUserId(userId);
            }
        });

        if (cacheListener != null) {
            try {
                cacheListener.subscribe(new SubscribeCallback() {
                    @Override
                    public void fire(byte[] data) {
                    CacheEvent m = CacheEvent.from(data);
                    if (Objects.equal(CacheMessage.USER_DATA_PERMISSION, CacheMessage.from(m.getEventType()))){
                        coreEventDispatcher.publish(new UserDataPermissionModifyEvent(m.getData()));
                    }
                }});
            } catch (Exception e) {
                log.error("failed to subscribe cache event, cause: {}", Throwables.getStackTraceAsString(e));
            }
        }
    }

    public void invalidateUserDataPermission(Long userId){
        permissionLoadingCache.invalidate(userId);
    }

    public DoctorUserDataPermission getUserDataPermission(Long userId){
        return permissionLoadingCache.getUnchecked(userId);
    }
}
