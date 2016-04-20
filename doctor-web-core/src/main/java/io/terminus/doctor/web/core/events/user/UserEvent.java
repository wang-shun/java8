/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.events.user;

import io.terminus.doctor.common.model.ParanaUser;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public abstract class UserEvent  implements Serializable {

    private static final long serialVersionUID = 7614783431931285762L;
    @Getter
    private HttpServletRequest request;

    @Getter
    private HttpServletResponse response;

    @Getter
    private final ParanaUser user;

    public UserEvent(HttpServletRequest request, HttpServletResponse response){
        this(request, response, null);
    }

    public UserEvent(HttpServletRequest request, HttpServletResponse response, ParanaUser user){
        this.request = request;
        this.response = response;
        this.user = user;
    }
}
