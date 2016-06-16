/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.event;


import io.terminus.doctor.user.model.DoctorFarm;
import lombok.Getter;

import java.util.List;

/**
 * Author:  陈增辉
 * Date: 2016-06-08
 * 用户猪场软件服务已开通 事件
 */
public class OpenDoctorServiceEvent {

    public OpenDoctorServiceEvent(Long userId, List<DoctorFarm> farms){
        this.farms = farms;
        this.userId = userId;
    }

    /**
     * 开通猪场软件时运营人员为用户添加的猪场
     */
    @Getter
    private List<DoctorFarm> farms;

    @Getter
    private Long userId;

}
