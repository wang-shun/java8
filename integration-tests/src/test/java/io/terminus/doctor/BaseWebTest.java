/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor;

import com.google.common.base.Joiner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-26
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest(randomPort=true)
public abstract class BaseWebTest {
    protected RestTemplate restTemplate = new TestRestTemplate();

    @Value("${local.server.port}")
    protected int port;

    /**
     * get请求 @RequestParam 格式
     * @param url  RequestMapping.value
     * @param params        请求参数
     * @param responseType  返回结果类
     * @param <T>  返回结果的泛型
     * @return 返回Http结果
     */
    protected <T> ResponseEntity<T> getForEntity(String url, Map<String, Object> params, Class<T> responseType) {
        return restTemplate.getForEntity(url(joinMap(url, params)), responseType);
    }

    protected <T> T getForObject(String url, Class<T> responseType) {
        return restTemplate.getForObject(url(url), responseType);
    }

    /**
     * post请求 @RequestBody 格式
     * @param url  RequestMapping.value
     * @param requestObject  RequestBody 的参数
     * @param responseType   返回结果类
     * @param <T>  返回结果的泛型
     * @return 返回Http结果
     */
    protected <T> ResponseEntity<T> postForEntity(String url, Object requestObject, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity entity = new HttpEntity<>(requestObject, httpHeaders);
        return restTemplate.postForEntity(url(url), entity, responseType);
    }

    protected <T> T postForObject(String url, Object requestObject, Class<T> responseType) {
        return postForEntity(url, requestObject, responseType).getBody();
    }

    /**
     * post请求 form表单格式
     * @param url   RequestMapping.value
     * @param form  参数
     * @param responseType  返回结果类
     * @param <T>  返回结果的泛型
     * @return  返回Http结果
     */
    protected <T> ResponseEntity<T> postFormForEntity(String url, Map<String, Object> form, Class<T> responseType) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(form.size());
        params.setAll(form);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, httpHeaders);
        return restTemplate.exchange(url(url), HttpMethod.POST, entity, responseType);
    }

    protected <T> T postFormForObject(String url, Map<String, Object> form, Class<T> responseType) {
        ResponseEntity<T> response = postFormForEntity(url, form, responseType);
        return response.getBody();
    }

    /**
     * put请求 form表单格式
     * @param url   RequestMapping.value
     * @param form  参数
     * @param responseType  返回结果类
     * @param <T>  返回结果的泛型
     * @return  返回Http结果
     */
    protected <T>ResponseEntity<T> putFormForEntity(String url, Map<String, Object> form, Class<T> responseType) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(form.size());
        params.setAll(form);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, httpHeaders);
        return restTemplate.exchange(url(url), HttpMethod.PUT, entity, responseType);
    }

    protected <T> T putFormForObject(String url, Map<String, Object> form, Class<T> responseType) {
        ResponseEntity<T> response = putFormForEntity(url, form, responseType);
        return response.getBody();
    }

    /**
     * delete请求
     * @param url   RequestMapping.value
     * @param params  参数
     */
    protected void delete(String url, Map<String, Object> params) {
        restTemplate.delete(url(joinMap(url, params)));
    }

     // 拼接map成 ?key1=value1&key2=value2 的格式
    private static String joinMap(String url, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            url += "?" + Joiner.on("&").withKeyValueSeparator("=").join(params);
        }
        return url;
    }

    protected String url(String url) {
        return "http://localhost:" + port + url;
    }
}
