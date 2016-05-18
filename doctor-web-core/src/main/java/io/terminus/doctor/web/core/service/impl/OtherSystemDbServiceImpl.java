package io.terminus.doctor.web.core.service.impl;

import com.google.common.base.Optional;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.web.core.enums.TargetSystem;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.parana.config.ConfigCenter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 陈增辉 16/5/18.
 */
public class OtherSystemDbServiceImpl implements OtherSystemService {

    private final ConfigCenter configCenter;

    @Autowired
    public OtherSystemDbServiceImpl(ConfigCenter configCenter){
        this.configCenter = configCenter;
    }

    @Override
    public TargetSystem setTargetSystemValue(TargetSystem targetSystem){
        targetSystem.setValueOfDomain(this.getConfigValue(targetSystem.getKeyOfDomain()));
        targetSystem.setValueOfPasword(this.getConfigValue(targetSystem.getKeyOfPasword()));
        targetSystem.setValueOfCorpId(Long.valueOf(this.getConfigValue(targetSystem.getKeyOfCorpId())));
        return targetSystem;
    }

    private String getConfigValue(String key){
        Optional<String> optional = configCenter.get(key);
        return optional.or(() -> {
            throw new ServiceException("required.config.is.missing");
        });
    }
}
