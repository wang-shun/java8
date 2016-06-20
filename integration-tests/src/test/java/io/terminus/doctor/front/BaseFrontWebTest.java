package io.terminus.doctor.front;

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
@ActiveProfiles({"test", "front"})
public abstract class BaseFrontWebTest extends BaseWebTest {
    protected RestTemplate restTemplate = new TestRestTemplate();
}
