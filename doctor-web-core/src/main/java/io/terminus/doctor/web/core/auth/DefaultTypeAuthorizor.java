/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.auth;

import io.terminus.doctor.web.core.auth.TypeAuthorizor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-13
 */
@Slf4j
public class DefaultTypeAuthorizor implements TypeAuthorizor {
    @Override
    public boolean matches(Set<String> expectedTypes, Integer actualType) {
        if(expectedTypes.contains("ALL")||expectedTypes.contains("USER")){
            return true;
        }
        switch (actualType){
            case 0:
                if(log.isDebugEnabled()){
                    log.debug("expected type: {}, actual type:{} ", expectedTypes, actualType);
                }
                return expectedTypes.contains("ADMIN");
            case 1:
                if(log.isDebugEnabled()){
                    log.debug("expected type: {}, actual type:{} ", expectedTypes, actualType);
                }
                return expectedTypes.contains("BUYER");
            case 2:
                if(log.isDebugEnabled()){
                    log.debug("expected type: {}, actual type:{} ", expectedTypes, actualType);
                }
                return expectedTypes.contains("SELLER");
        }
        return false;
    }
}
