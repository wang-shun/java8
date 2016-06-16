package io.terminus.doctor.user.event;

import io.terminus.doctor.common.event.Event;

/**
 * Desc: 用户的员工信息变更事件
 * author:陈增辉
 * Date: 2016年06月14日
 */
public class UserStaffInfoModifyEvent extends Event<Long> {
    public UserStaffInfoModifyEvent(Long userId){
        super(userId);
    }
    public UserStaffInfoModifyEvent(){

    }
}
