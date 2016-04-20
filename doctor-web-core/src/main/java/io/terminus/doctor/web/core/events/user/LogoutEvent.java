/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.events.user;

import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.doctor.web.core.events.user.UserEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public class LogoutEvent extends UserEvent {

    public LogoutEvent(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public LogoutEvent(HttpServletRequest request, HttpServletResponse response, ParanaUser user) {
        super(request, response, user);
    }
}
