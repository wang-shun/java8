/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.events.user;

import lombok.Getter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-31
 */
public class UserActivateEvent implements Serializable {
    private static final long serialVersionUID = 7806570333928419259L;

    @Getter
    private final Long userId;

    public UserActivateEvent(Long userId) {
        this.userId = userId;
    }
}
