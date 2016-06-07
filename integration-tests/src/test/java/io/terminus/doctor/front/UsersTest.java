package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.user.model.DoctorUser;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Desc: 前段测试
 * Mail: houly@terminus.io
 * Data: 上午10:23 16/5/31
 * Author: houly
 */
@SpringApplicationConfiguration(value = {FrontWebConfiguration.class})
public class UsersTest extends BaseFrontWebTest {

    @Test
    public void userInfoTest(){
        ResponseEntity<DoctorUser> result = restTemplate.getForEntity("http://localhost:{port}/api/user",
                DoctorUser.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }


    @Test
    public void login(){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.put("loginBy", Lists.newArrayList("admin"));
        map.put("password", Lists.newArrayList("123456"));
        map.put("type", Lists.newArrayList("1"));
        ResponseEntity<Map> result = restTemplate.postForEntity("http://localhost:{port}/api/user/login",
                map ,Map.class, ImmutableMap.of("port", port));

        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}
