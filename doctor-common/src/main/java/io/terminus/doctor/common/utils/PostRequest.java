package io.terminus.doctor.common.utils;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;


public class PostRequest {
    public static void main(String[] arg){
        List<NameValuePair> nameValueList = new ArrayList<>();
        nameValueList.add(new NameValuePair() {
            @Override
            public String getName() {
                return "pigGroupId";
            }

            @Override
            public String getValue() {
                return "38965";
            }
        });
        nameValueList.add(new NameValuePair() {
            @Override
            public String getName() {
                return "newQuantity";
            }

            @Override
            public String getValue() {
                return "25";
            }
        });
        String url = "api/iot/pig/group-stock-change";
        httpPostWithJson(nameValueList,url,null);
    }


    //////////////////////

    public static boolean httpPostWithJson(List<NameValuePair> nameValuePairList, String url, String appId){
        boolean isSuccess = false;

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);

            post = new HttpPost("http://swagger.iot-test.xrnm.com/"+url);
            // 构造消息头
            //post.setHeader("Content-Type", "application/json");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setHeader("Accept", "*/*");
            post.setHeader("Connection", "Keep-Alive");
            post.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            //post.setHeader("Connection", "Close");
            /*String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", appid);*/

            // 构建消息实体
           /* String str = jsonObj.toString();
            StringEntity entity = new StringEntity(str, Charset.forName("UTF-8"));*/
            //entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            //entity.setContentType("application/json");
            post.setEntity(new UrlEncodedFormEntity(nameValuePairList,"UTF-8"));

            HttpResponse response = httpClient.execute(post);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                if (statusCode == 302) {
                    Header location = response.getFirstHeader("location");
                    String newUrl = location.getValue();
                    System.out.println(newUrl);

                    HttpPost newPost = new HttpPost(newUrl);
                    newPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    newPost.setHeader("Accept", "*/*");
                    newPost.setHeader("Connection", "Keep-Alive");
                    newPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

                    newPost.setEntity(new UrlEncodedFormEntity(nameValuePairList,"UTF-8"));
                    HttpResponse newResponse = httpClient.execute(newPost);
                    int statusCode1 = newResponse.getStatusLine().getStatusCode();
                    if (statusCode1 != HttpStatus.SC_OK) {
                        System.out.println("请求出错: "+statusCode);
                        isSuccess = false;
                    }
                    else {
                        System.out.println("success");

                    }

                }
                System.out.println("请求出错: "+statusCode);
                isSuccess = false;
            }else{
                int retCode = 0;
                String sessendId = "";

            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }finally{
            if(post != null){
                try {
                    // post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }
}
