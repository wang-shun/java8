package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import utils.HttpGetRequest;

/**
 * Desc: 消息中心
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@SpringApplicationConfiguration(value = {FrontWebConfiguration.class})
public class DoctorMessagesTest extends BaseFrontWebTest {

    /**
     * 未读消息数量查询
     */
    @Test
    public void test_NO_READ_DoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/noReadCount").build();
        Long count = this.restTemplate.getForObject(url, Long.class, ImmutableMap.of("port", this.port));
        System.out.println(count);
    }

    // test_PAGE_WarnDoctorMessages

}
