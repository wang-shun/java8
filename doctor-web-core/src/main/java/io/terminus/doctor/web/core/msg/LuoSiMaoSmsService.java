package io.terminus.doctor.web.core.msg;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.lib.sms.SmsException;
import io.terminus.lib.sms.SmsService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 螺丝帽短信的实现
 * Mail: houly@terminus.io
 * Data: 下午4:45 16/5/26
 * Author: houly
 */
@Slf4j
public class LuoSiMaoSmsService implements SmsService {
    private JsonMapper jsonMapper = JsonMapper.JSON_NON_EMPTY_MAPPER;

    private final LuoSiMaoToken luoSiMaoToken;


    public LuoSiMaoSmsService(String sendUrl, String statusUrl, String apiKey, String companyName){
        luoSiMaoToken = LuoSiMaoToken.builder().apiKey(apiKey)
                .companyName(companyName)
                .sendUrl(sendUrl)
                .statusUrl(statusUrl)
                .build();
    }

    @Override
    public String send(String from, String toes, String message, String extra) throws SmsException {
        return send(from,toes,message);
    }

    @Override
    public String send(String from, String toes, String message) throws SmsException {
        toes=singleString2JsonFormat(toes);

        List<String> toList= JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(toes, List.class);

        for(String to : toList){
            String result = doSendSms(from, to, message);
            if(!Objects.equals(result, "0")){
                log.error("failed to send message from {} to {},error code:{}", from, to, result);
                throw new ServiceException(result);
            }
        }
        return "0";
    }

    private String doSendSms(String from, String to, String message){
        //接口调用 GET
        String toMessage = message+luoSiMaoToken.getCompanyName();
        Map<String, String> params = ImmutableMap.of("key", luoSiMaoToken.getApiKey(), "mobile", to,"message",toMessage);
        String body = HttpRequest.get(luoSiMaoToken.getSendUrl(), params, true).body();

        //结果解析
        Map<String, String> res = jsonMapper.fromJson(body, jsonMapper.createCollectionType(HashMap.class, String.class, String.class));
        if(Objects.equals(res.get("error"),"0")){
            return "0";
        }else{
            return res.get("msg");
        }
    }

    @Override
    public Integer available() {
        //接口调用 GET
        Map<String, String> params = ImmutableMap.of("key", luoSiMaoToken.getApiKey());
        String body = HttpRequest.get(luoSiMaoToken.getStatusUrl(), params, true).body();

        //校验返回结果内容
        Map<String,Object> res = jsonMapper.fromJson(body,Map.class);
        if(Objects.equals(res.get("error"),"0")){
            return Integer.valueOf(String.valueOf(res.get("deposit")));
        }else{
            log.error("message send fail, error:{}", String.valueOf(res.get("deposit")));
            return null;
        }
    }

    protected String singleString2JsonFormat(String from){
        if(!from.contains("[")){
            from="[\""+from+"\"]";
        }
        return from;
    }
}
