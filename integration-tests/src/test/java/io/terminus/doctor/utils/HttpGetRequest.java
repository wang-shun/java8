package io.terminus.doctor.utils;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe: 构建HTTP get request info
 */
public class HttpGetRequest {

    private StringBuilder urlRequest;

    private boolean isFirst;

    private HttpGetRequest(){}

    public class HttpGetRequestBuilder{

        public HttpGetRequestBuilder params(String key, Object value){
            if(isFirst){
                urlRequest.append("?").append(key).append("=").append(value);
                isFirst = false;
            }else {
                urlRequest.append("&").append(key).append("=").append(value);
            }
            return this;
        }

        public String build(){
            return urlRequest.toString();
        }
    }

    public static HttpGetRequestBuilder url(String url){
        HttpGetRequest httpGetRequest = new HttpGetRequest();
        httpGetRequest.urlRequest = new StringBuilder(url);
        httpGetRequest.isFirst = true;
        return httpGetRequest.new HttpGetRequestBuilder();
    }
}
