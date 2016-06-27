package io.terminus.doctor.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-03
 * Email:yaoqj@terminus.io
 * Descirbe: http get 请求 Body Form 格式的封装
 */
public class HttpPostRequest {

    private HttpHeaders requestHeaders = null;

    private HttpPostRequest(){}

    public class HttpPostRequestFormBuild{

        private MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();

        public HttpPostRequestFormBuild param(String key, Object value){
            multiValueMap.add(key, value);
            return this;
        }

        public HttpPostRequestFormBuild params(String key, List<Object> values){
            multiValueMap.put(key, values);
            return this;
        }

        public HttpEntity httpEntity(){
            return new HttpEntity(multiValueMap, requestHeaders);
        }
    }

    public class HttpPostRequestBuild{
        public HttpEntity params(Object body){
            return new HttpEntity(body, requestHeaders);
        }
    }

    public static HttpPostRequestBuild bodyRequest(){
        HttpPostRequest httpPostRequest = new HttpPostRequest();
        httpPostRequest.requestHeaders = new HttpHeaders();
        httpPostRequest.requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return httpPostRequest.new HttpPostRequestBuild();
    }

    public static HttpPostRequestFormBuild formRequest(){
        HttpPostRequest httpPostRequest = new HttpPostRequest();
        httpPostRequest.requestHeaders = new HttpHeaders();
        httpPostRequest.requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return httpPostRequest.new HttpPostRequestFormBuild();
    }
}
