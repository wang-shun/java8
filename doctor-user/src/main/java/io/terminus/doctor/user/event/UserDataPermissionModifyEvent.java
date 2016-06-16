package io.terminus.doctor.user.event;

import io.terminus.doctor.common.event.Event;

/**
 * Desc: 用户数据权限变更事件
 * author:陈增辉
 * Date: 2016年06月14日
 */
public class UserDataPermissionModifyEvent extends Event<Long> {
    public UserDataPermissionModifyEvent(Long userId){
        super(userId);
    }
    public UserDataPermissionModifyEvent(){

    }
}
