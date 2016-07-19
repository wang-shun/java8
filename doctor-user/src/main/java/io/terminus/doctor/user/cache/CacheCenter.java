package io.terminus.doctor.user.cache;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.common.event.CacheEvent;
import io.terminus.doctor.common.event.CacheMessage;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.event.UserDataPermissionModifyEvent;
import io.terminus.doctor.user.event.UserStaffInfoModifyEvent;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.zookeeper.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

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
    private final DoctorStaffDao doctorStaffDao;

    @Autowired
    public CacheCenter(DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                       DoctorStaffDao doctorStaffDao){
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorStaffDao = doctorStaffDao;
    }

    //cache, key = userId, value = permission
    private LoadingCache<Long, DoctorUserDataPermission> permissionLoadingCache;
    //cache, key = userId, value = DoctorOrg
    private LoadingCache<Long, DoctorStaff> staffLoadingCache;

    @PostConstruct
    public void initCache(){
        //初始化数据权限缓存对象
        permissionLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.MINUTES).build(new CacheLoader<Long, DoctorUserDataPermission>() {
            @Override
            public DoctorUserDataPermission load(Long userId) throws Exception {
                return doctorUserDataPermissionDao.findByUserId(userId);
            }
        });

        //初始化员工信息缓存对象
        staffLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.MINUTES).build(new CacheLoader<Long, DoctorStaff>() {
            @Override
            public DoctorStaff load(Long userId) throws Exception {
                return doctorStaffDao.findByUserId(userId);
            }
        });

        //初始化zookeeper监听器
        if (cacheListener != null) {
            try {
                cacheListener.subscribe(data -> {
                CacheEvent m = CacheEvent.from(data);
                if (Objects.equal(CacheMessage.USER_DATA_PERMISSION.getValue(), m.getEventType())){
                    coreEventDispatcher.publish(new UserDataPermissionModifyEvent(m.getData()));
                } else if (Objects.equal(CacheMessage.USER_STAFF_INFO.getValue(), m.getEventType())){
                    coreEventDispatcher.publish(new UserStaffInfoModifyEvent(m.getData()));
                }
            });
            } catch (Exception e) {
                log.error("failed to subscribe cache event, cause: {}", Throwables.getStackTraceAsString(e));
            }
        }
    }

    public void invalidateUserDataPermission(Long userId){
        permissionLoadingCache.invalidate(userId);
    }

    public DoctorUserDataPermission getUserDataPermission(Long userId){
        try{
            return permissionLoadingCache.get(userId);
        }catch(Exception e){
            return null;
        }
    }

    public void invalidateStaff(Long userId){
        staffLoadingCache.invalidate(userId);
    }

    public DoctorStaff getStaff(Long userId){
        try{
            return staffLoadingCache.get(userId);
        }catch(Exception e){
            return null;
        }
    }
}
