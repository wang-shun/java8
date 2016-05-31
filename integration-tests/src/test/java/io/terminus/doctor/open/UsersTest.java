package io.terminus.doctor.open;

import com.google.common.collect.ImmutableMap;
import configuration.open.OpenWebConfiguration;
import io.terminus.doctor.BaseWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Desc: open测试
 * Mail: houly@terminus.io
 * Data: 上午10:23 16/5/31
 * Author: houly
 */
@SpringApplicationConfiguration({OpenWebConfiguration.class})
public class UsersTest extends BaseOpenWebTest {
    private RestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void userInfoTest(){
        ResponseEntity<Map> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway?pampasCall=get.mobile.code&mobile=18661744610&sid=test&appKey=pigDoctorAndroid&sign=30a46c341621c7d4301c5fb6a738dc41",
                Map.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}
