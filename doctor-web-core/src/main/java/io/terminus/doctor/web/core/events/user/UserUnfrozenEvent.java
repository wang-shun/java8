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
public class UserUnfrozenEvent implements Serializable {
    private static final long serialVersionUID = 8800453857299602788L;
    @Getter
    private final Long userId;

    @Getter
    private final Long operatorId;

    public UserUnfrozenEvent(Long userId, Long operatorId) {
        this.userId = userId;
        this.operatorId = operatorId;
    }
}
