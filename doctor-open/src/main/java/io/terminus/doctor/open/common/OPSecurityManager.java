package io.terminus.doctor.open.common;

import com.google.common.collect.Maps;
import io.terminus.pampas.openplatform.core.SecurityManager;
import io.terminus.pampas.openplatform.entity.OPClientInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-09 8:07 PM  <br>
 * Author: xiao
 */
@Component
public class OPSecurityManager implements SecurityManager {

    private final Map<String, OPClientInfo> clintMap;

    public OPSecurityManager() {
        clintMap = Maps.newHashMap();
        clintMap.put("pigDoctorAndroid", new OPClientInfo(1L, "pigDoctorAndroid", "pigDoctorAndroidSecret"));
        clintMap.put("pigDoctorIOS", new OPClientInfo(2L, "pigDoctorIOS", "pigDoctorIOSSecret"));
    }

    @Override
    public OPClientInfo findClientByAppKey(String appKey) {
        // TODO: 16/5/18 这里要放到数据库里
        return clintMap.get(appKey);
    }

    @Override
    public boolean hasPermission(Long clientId, String method) {
        // TODO: 16/5/18 数据库里要配上有权限的method
        
        return true;
    }
}
