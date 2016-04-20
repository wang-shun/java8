/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.auth;

import io.terminus.doctor.web.core.auth.RoleAuthorizor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-13
 */
@Slf4j
public class DefaultRoleAuthorizor implements RoleAuthorizor {
    @Override
    public boolean matches(Set<String> expectedRoles, List<String> actualRoles) {
        if(CollectionUtils.isEmpty(expectedRoles)){    //no expected roles
            return true;
        }
        if(CollectionUtils.isEmpty(actualRoles)){
            return false;
        }
        for (String expectedRole : expectedRoles) {
            if(actualRoles.contains(expectedRole)){
                return true;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("expected roles: {}, actual roles:{} ", expectedRoles, actualRoles);
        }
        return false;
    }
}
