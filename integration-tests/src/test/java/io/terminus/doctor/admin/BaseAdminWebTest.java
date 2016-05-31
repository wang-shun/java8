package io.terminus.doctor.admin;

import io.terminus.doctor.BaseWebTest;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

/**
 * Desc:
 * Mail: houly@terminus.io
 * Data: 下午6:46 16/5/31
 * Author: houly
 */
@ActiveProfiles({"test", "admin"})
public abstract class BaseAdminWebTest extends BaseWebTest {
    protected RestTemplate restTemplate = new TestRestTemplate();
}
