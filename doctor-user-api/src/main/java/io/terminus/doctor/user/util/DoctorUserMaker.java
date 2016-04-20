/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.util;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.parana.user.model.User;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public abstract class DoctorUserMaker {
    public static ParanaUser from(User user){
        ParanaUser paranaUser = new ParanaUser();
        BeanMapper.copy(user, paranaUser);
        return paranaUser;
    }
}
