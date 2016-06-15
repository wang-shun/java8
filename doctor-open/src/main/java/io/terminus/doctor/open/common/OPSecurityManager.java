package io.terminus.doctor.open.common;

import com.google.common.collect.Maps;
import io.terminus.pampas.openplatform.core.OPHook;
import io.terminus.pampas.openplatform.core.SecurityManager;
import io.terminus.pampas.openplatform.entity.OPClientInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-09 8:07 PM  <br>
 * Author: xiao
 */
@Component
public class OPSecurityManager implements SecurityManager {

    private final Map<String, OPClientInfo> clientMap;

    private final Map<Long, OPClientInfo> idClientMap;

    public OPSecurityManager() {
        clientMap = Maps.newHashMap();
        clientMap.put("pigDoctorAndroid", new OPClientInfo(1L, "pigDoctorAndroid", "pigDoctorAndroidSecret"));
        clientMap.put("pigDoctorIOS", new OPClientInfo(2L, "pigDoctorIOS", "pigDoctorIOSSecret"));

        idClientMap = clientMap.values().stream().collect(Collectors.toMap(o->o.getClientId(), o->o));
    }

    @Override
    public OPClientInfo findClientByAppKey(String appKey) {
        // TODO: 16/5/18 这里要放到数据库里
        return clientMap.get(appKey);
    }

    @Override
    public OPClientInfo findClientById(Long aLong) {
        return idClientMap.get(aLong);
    }

    @Override
    public boolean hasPermission(Long clientId, String method) {
        // TODO: 16/5/18 数据库里要配上有权限的method
        return true;
    }

    @Override
    public OPHook getHook(Long aLong, String s) {
        return null;
    }
}
