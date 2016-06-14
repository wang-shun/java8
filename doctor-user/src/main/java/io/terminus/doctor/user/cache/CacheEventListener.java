package io.terminus.doctor.user.cache;

import com.google.common.eventbus.Subscribe;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.user.event.UserDataPermissionModifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:缓存更新事件监听器
 * author: 陈增辉
 * Date: 2016年06月14日
 */
@Slf4j
@Component
public class CacheEventListener implements EventListener {
    private final CacheCenter cacheCenter;

    @Autowired
    public CacheEventListener(CacheCenter cacheCenter){
        this.cacheCenter = cacheCenter;
    }

    @Subscribe
    public void onUserDataPermissionEvent(UserDataPermissionModifyEvent e){
        log.info("user data permission modify event catched, userId:{}", e.getData());
        if(Arguments.notNull(e.getData())){
            cacheCenter.invalidateUserDataPermission(e.getData());
        }
    }

}
