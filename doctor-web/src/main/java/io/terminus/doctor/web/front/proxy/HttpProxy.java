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
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Created by yudi on 2016/11/16.
 * Mail to yd@terminus.io
 */
@RestController
@RequestMapping("/api/gateway/proxy")
@Slf4j
public class HttpProxy {
    private final String APP_KEY="pigDoctorPC";
    private final String APP_SECRET="pigDoctorPCSecret";
    @Value("${openApi.url}")
    private  String urlConf="";
    @RequestMapping(value = "",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String proxy(String pampasCall,String sid){
        if (Arguments.isEmpty(pampasCall)){
            throw new JsonResponseException("param.not.allow.null");
        }
        List<String> paramList= Lists.newArrayList();
        paramList.add("pampasCall="+pampasCall);
        paramList.add("appKey="+APP_KEY);
        paramList.add("timestamp="+System.currentTimeMillis());
        paramList.add("sid="+sid);
        paramList.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
       String param= StringUtils.join(paramList,"&");
       String sign= Hashing.md5().hashString(param+APP_SECRET, Charsets.UTF_8).toString();
        param+="&sign="+sign;
        String url=urlConf+"/api/gateway?"+param;
        log.info(url);
        String result=new HttpRequest(url,"GET").body();
        log.info(result);
        return result;
    }
}
