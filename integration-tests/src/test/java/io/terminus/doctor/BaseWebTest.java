/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
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

    protected <T> T getForObject(String url, Class<T> responseType) {
        return restTemplate.getForObject(url(url), responseType);
    }

    protected <T> ResponseEntity<T> postForEntity(String url, Object requestObject, Class<T> responseType) {
        return restTemplate.postForEntity(url(url), requestObject, responseType);
    }

    protected <T> T postForObject(String url, Object requestObject, Class<T> responseType) {
        return restTemplate.postForObject(url(url), requestObject, responseType);
    }

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

    private String url(String url) {
        return "http://localhost:" + port + url;
    }
}
