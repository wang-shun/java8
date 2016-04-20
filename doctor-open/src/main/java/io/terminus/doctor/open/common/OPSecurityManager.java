package io.terminus.doctor.open.common;

import io.terminus.pampas.openplatform.core.SecurityManager;
import io.terminus.pampas.openplatform.entity.OPClientInfo;
import org.springframework.stereotype.Component;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-09 8:07 PM  <br>
 * Author: xiao
 */
@Component
public class OPSecurityManager implements SecurityManager {




    @Override
    public OPClientInfo findClientByAppKey(String s) {
        return new OPClientInfo(1L, "xxxxx", "xxxxx");
    }

    @Override
    public boolean hasPermission(Long aLong, String s) {
        return true;
    }
}
