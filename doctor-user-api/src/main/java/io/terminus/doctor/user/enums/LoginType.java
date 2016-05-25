/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.enums;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-28
 */
public enum LoginType {
    NAME(1),
    EMAIL(2),
    MOBILE(3),
    SUBACCOUNT(4),
    OTHER(4);

    private final int type;

    LoginType(int type) {
        this.type = type;
    }

    public static LoginType from(int value){
        for (LoginType loginType : LoginType.values()) {
            if(loginType.type == value){
                return loginType;
            }
        }
        throw new IllegalArgumentException("illegal login type: "+ value);
    }
}
