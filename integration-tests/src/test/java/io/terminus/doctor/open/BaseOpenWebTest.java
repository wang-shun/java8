package io.terminus.doctor.open;

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
@ActiveProfiles({"test", "open"})
public abstract class BaseOpenWebTest extends BaseWebTest {
    protected RestTemplate restTemplate = new TestRestTemplate();
}
