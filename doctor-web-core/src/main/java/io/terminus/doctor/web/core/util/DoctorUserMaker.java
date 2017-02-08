/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.util;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.service.DoctorServiceStatusReadService;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.model.User;
import org.springframework.stereotype.Component;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
@Component
public class DoctorUserMaker {

    @RpcConsumer
    private DoctorServiceStatusReadService doctorServiceStatusReadService;

    public ParanaUser from(User user){
        DoctorUser doctorUser = BeanMapper.map(user, DoctorUser.class);

        //设置下猪场软件服务的审核状态  0 未申请, 2 待审核(已提交申请), 1 通过，-1 不通过, -2 冻结申请资格
        Response<DoctorServiceStatus> response = doctorServiceStatusReadService.findByUserId(user.getId());
        if (response.isSuccess() && response.getResult() != null) {
            doctorUser.setReviewStatusDoctor(response.getResult().getPigdoctorReviewStatus());
        }
        return doctorUser;
    }
}
