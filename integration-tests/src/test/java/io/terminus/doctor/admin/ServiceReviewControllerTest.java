package io.terminus.doctor.admin;

import com.google.common.collect.ImmutableMap;
import configuration.admin.AdminWebConfiguration;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.web.admin.dto.UserApplyServiceDetailDto;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Desc: 后端测试
 * Mail: houly@terminus.io
 * Data: 上午10:23 16/5/31
 * Author: houly
 */
@SpringApplicationConfiguration({AdminWebConfiguration.class})
public class ServiceReviewControllerTest extends BaseAdminWebTest {
    @Test
    public void testPage(){
        ResponseEntity<UserApplyServiceDetailDto> result = restTemplate.getForEntity("http://localhost:{port}/api/doctor/admin/service?fuck=1",
                UserApplyServiceDetailDto.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        System.out.println(result.getBody());
    }
}
