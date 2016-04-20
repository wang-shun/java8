package io.terminus.doctor.open.sample;

import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.pampas.openplatform.utils.ClientUtil;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.config.model.Config;
import io.terminus.parana.config.service.ConfigWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-09 8:00 PM  <br>
 * Author: xiao
 */
@Slf4j
@OpenBean
public class OPSamples {

    @Autowired
    private ConfigCenter configCenter;

    @Autowired
    private ConfigWriteService configWriteService;


    @OpenMethod(key="say.hi", paramNames = {"name"})
    public String sayHi(@NotNull(message = "name.not.null") String name){
        return "Hi! "+name+", your appKey is: "
                + ClientUtil.get().getAppKey()+" , your clientId is :"
                + ClientUtil.get().getClientId();
    }




    @OpenMethod(key="ping", paramNames = {"name"})
    public String sayPong(String name) {
        log.info("secret : {}", configCenter.get("bad.word.main"));
        return name;
    }

    @OpenMethod(key="get.config", paramNames = {"key"})
    public String getConfig(String key) {
        log.info("secret : {}", configCenter.get(key));
        if (!configCenter.get(key).isPresent()) {
            throw new OPClientException(400, "key not existed");
        }

        return configCenter.get(key).get();
    }

    @OpenMethod(key="up.config", paramNames = {"op", "key", "value", "id"})
    public String updateConfig(String op, String key, String value, Long id) {
        if ("create".equals(op)) {
            Config config = new Config();
            config.setKey(key);
            config.setValue(value);
            configWriteService.create(config);
        }

        if ("update".equals(op)) {
            Config config = new Config();
            config.setId(id);
            config.setKey(key);
            config.setValue(value);
            configWriteService.update(config);
        }

        if ("delete".equals(op)) {
            configWriteService.delete(id);
        }

        log.info("secret : {}", configCenter.get(key));
        return configCenter.get(key).get();
    }


    @OpenMethod(key="wtf", paramNames = {})
    public void sayError() {
        throw new OPClientException(500, "invoke.error");
    }
}
