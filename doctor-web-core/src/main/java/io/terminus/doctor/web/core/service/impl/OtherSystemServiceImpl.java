package io.terminus.doctor.web.core.service.impl;

import com.google.common.base.Optional;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.TargetSystemModel;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.parana.config.ConfigCenter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 陈增辉 16/5/18.
 */
public class OtherSystemServiceImpl implements OtherSystemService {

    private final ConfigCenter configCenter;

    @Autowired
    public OtherSystemServiceImpl(ConfigCenter configCenter){
        this.configCenter = configCenter;
    }

    @Override
    public TargetSystemModel getTargetSystemModel(TargetSystem targetSystem){
        TargetSystemModel model = new TargetSystemModel();
        String domain = this.getConfigValue(targetSystem.domain());
        if (domain.endsWith("/")) {
            model.setDomain(domain.substring(0, domain.lastIndexOf("/")));
        } else {
            model.setDomain(domain);
        }
        model.setPassword(this.getConfigValue(targetSystem.password()));
        model.setCorpId(Long.valueOf(this.getConfigValue(targetSystem.corpId())));
        return model;
    }

    private String getConfigValue(String key){
        Optional<String> optional = configCenter.get(key);
        return optional.or(() -> {
            throw new ServiceException("required.config.missing");
        });
    }
}
