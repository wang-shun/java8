/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.auth;

import java.util.Set;

/**
 * 基于用户类型的鉴权
 *
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-13
 */
public interface TypeAuthorizor {

    /**
     * 判断用户类型是否匹配
     *
     * @param expectedTypes  鉴权要求的类型, 字符串形式表示
     * @param actualType     用户实际类型, 整型表示
     * @return  用户类型是否匹配
     */
    boolean matches(Set<String> expectedTypes, Integer actualType);
}
