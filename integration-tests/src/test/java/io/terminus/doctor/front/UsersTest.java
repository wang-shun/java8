package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.user.model.DoctorUser;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
}
