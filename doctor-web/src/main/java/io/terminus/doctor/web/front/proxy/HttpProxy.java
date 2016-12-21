package io.terminus.doctor.web.front.proxy;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by yudi on 2016/11/16.
 * Mail to yd@terminus.io
 */
@RestController
@RequestMapping("/api/gateway/proxy")
@Slf4j
public class HttpProxy {
    private static final String APP_KEY="pigDoctorPC";
    private static final String APP_SECRET="pigDoctorPCSecret";

    @Value("${openApi.url:}")
    private String urlConf;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String proxy(@RequestParam String pampasCall,
                        @RequestParam String sid,
                        @RequestParam Map<String,String> params){
        if (Arguments.isEmpty(pampasCall)){
            throw new JsonResponseException("param.not.allow.null");
        }
        if (Arguments.isEmpty(sid)){
            throw new JsonResponseException("sid.not.allow.null");
        }
        List<String> paramList = Lists.newArrayList();
        paramList.add("appKey=" + APP_KEY);
        paramList.add("timestamp=" + System.currentTimeMillis());
        if (params!=null){
            params.forEach((k,v) -> paramList.add(k+"="+v));
        }
        paramList.sort(Comparator.comparing(String::toLowerCase));
        String param= StringUtils.join(paramList,"&");
        String sign= Hashing.md5().hashString(param+APP_SECRET, Charsets.UTF_8).toString();
        param+="&sign="+sign;
        String url=urlConf+"/api/gateway?"+param;
        log.info("open api url:{}", url);
        String result = HttpRequest.get(url).trustAllCerts().trustAllHosts().body();
        log.info("open api result:{}", result);
        return result;
    }
}
