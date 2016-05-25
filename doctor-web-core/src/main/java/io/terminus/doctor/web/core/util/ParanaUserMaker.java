/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.util;

import io.terminus.common.utils.BeanMapper;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.model.User;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public abstract class ParanaUserMaker {
    public static final ParanaUser from(User user){
        ParanaUser paranaUser = new ParanaUser();
        BeanMapper.copy(user, paranaUser);
        return paranaUser;
    }
}
