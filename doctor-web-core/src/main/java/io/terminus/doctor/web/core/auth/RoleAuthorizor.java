/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.auth;

import java.util.List;
import java.util.Set;

/**
 * 基于角色的鉴权
 *
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-13
 */
public interface RoleAuthorizor {

    /**
     * 判断角色是否匹配
     *
     * @param expectedRoles  鉴权要求的角色
     * @param actualRole     用户实际具有的角色
     * @return  角色是否匹配
     */
    boolean matches(Set<String> expectedRoles, List<String> actualRole);
}
