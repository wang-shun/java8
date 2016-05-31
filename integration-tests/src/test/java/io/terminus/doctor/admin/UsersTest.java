package io.terminus.doctor.admin;

import com.google.common.collect.ImmutableMap;
import configuration.admin.AdminWebConfiguration;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.BaseWebTest;
import io.terminus.doctor.user.model.DoctorUser;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Desc: 后端测试
 * Mail: houly@terminus.io
 * Data: 上午10:23 16/5/31
 * Author: houly
 */
@SpringApplicationConfiguration({AdminWebConfiguration.class})
public class UsersTest extends BaseAdminWebTest {
    @Test
    public void userInfoTest(){
        ResponseEntity<DoctorUser> result = restTemplate.getForEntity("http://localhost:{port}/api/user",
                DoctorUser.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}
