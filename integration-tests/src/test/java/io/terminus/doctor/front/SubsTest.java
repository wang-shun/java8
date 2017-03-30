package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import configuration.front.FrontPrimaryWebConfiguration;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.web.front.role.Sub;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Desc: 子账号相关测试
 * Mail: houly@terminus.io
 * Data: 下午7:15 16/6/6
 * Author: houly
 */
@SpringApplicationConfiguration(value = {FrontPrimaryWebConfiguration.class})
public class SubsTest extends BaseFrontWebTest{
    @Test
    public void pagingSubs(){
        ResponseEntity<Paging> result= restTemplate.getForEntity("http://localhost:{port}/api/sub/pagination",  Paging.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getTotal(), not(0));
    }

    @Test
    public void createSub(){
        Sub sub = mockSub();
        ResponseEntity<Long> result = restTemplate.postForEntity("http://localhost:{port}/api/sub", sub,  Long.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(4L));
    }

    @Test
    public void updateSub(){
        Sub sub = mockSub();
        sub.setId(3L);
        restTemplate.put("http://localhost:{port}/api/sub", sub, ImmutableMap.of("port", port));
    }

    @Test
    public void resetPassword(){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.put("loginBy", Lists.newArrayList("18888888890@18888888889"));
        map.put("password", Lists.newArrayList("123456"));
        ResponseEntity<Map> result = restTemplate.postForEntity("http://localhost:{port}/api/user/login",
                map ,Map.class, ImmutableMap.of("port", port));

        assertThat(result.getStatusCode(), is(HttpStatus.OK));


        String resetPassword = "1234567";
        map.put("password", Lists.newArrayList(resetPassword));
        map.put("resetPassword", Lists.newArrayList(resetPassword));
        ResponseEntity<Boolean> resetResp = restTemplate.postForEntity("http://localhost:{port}/api/sub/reset/{userId}", map, Boolean.class, ImmutableMap.of("port", port, "userId", 3));

        assertThat(resetResp.getStatusCode(), is(HttpStatus.OK));
        assertThat(resetResp.getBody(), is(Boolean.TRUE));

        result = restTemplate.postForEntity("http://localhost:{port}/api/user/login",
                map ,Map.class, ImmutableMap.of("port", port));

        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void info(){
        ResponseEntity<Sub> result= restTemplate.getForEntity("http://localhost:{port}/api/sub/{userId}",  Sub.class, ImmutableMap.of("port", port, "userId", "3"));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }


    private Sub mockSub(){
        Sub sub = new Sub();
        sub.setUsername("abc");
        sub.setRealName("真实姓名");
        sub.setContact("18661744610");
        sub.setRoleId(1L);
        sub.setPassword("123456");
        System.out.println(ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(sub));
        return sub;
    }
}
