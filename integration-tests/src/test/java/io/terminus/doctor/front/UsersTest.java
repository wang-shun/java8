package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import configuration.front.FrontPrimaryWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.constant.Front;
import io.terminus.doctor.user.model.DoctorUser;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
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
@SpringApplicationConfiguration(value = {FrontPrimaryWebConfiguration.class})
public class UsersTest extends BaseFrontWebTest {

    @Test
    public void userInfoTest(){
        ResponseEntity<DoctorUser> result = restTemplate.getForEntity("http://localhost:{port}/api/user",
                DoctorUser.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        System.out.println(JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(result.getBody()));
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

    @Test
    public void imgVerify(){
        ResponseEntity<byte[]> result = restTemplate.getForEntity("http://localhost:{port}/api/user/imgVerify",
                byte[].class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void sms(){
        ResponseEntity<Boolean> result = restTemplate.getForEntity("http://localhost:{port}/api/user/sms?mobile={mobile}&imgVerify={imgVerify}",
                Boolean.class, ImmutableMap.of("port", port, "mobile", "18661744610", "imgVerify", Front.SESSION_IMG_CODE));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void register(){

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.put("mobile", Lists.newArrayList(Front.MOBILE));
        map.put("password", Lists.newArrayList("123456"));
        map.put("code", Lists.newArrayList(Front.SESSION_MSG_CODE));
        ResponseEntity<Long> result = restTemplate.postForEntity("http://localhost:{port}/api/user/register",
                map ,Long.class, ImmutableMap.of("port", port));

        assertThat(result.getStatusCode(), is(HttpStatus.OK));


        MultiValueMap<String, String> loginmap = new LinkedMultiValueMap<String, String>();
        loginmap.put("loginBy", Lists.newArrayList(Front.MOBILE));
        loginmap.put("password", Lists.newArrayList("123456"));
        ResponseEntity<Map> loginresult = restTemplate.postForEntity("http://localhost:{port}/api/user/login",
                loginmap ,Map.class, ImmutableMap.of("port", port));

        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

}
