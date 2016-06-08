package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableList;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import utils.HttpPostRequest;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/8
 */
@SpringApplicationConfiguration(value = {FrontWebConfiguration.class})
public class DoctorMsgRulesTest extends BaseFrontWebTest {

    /**
     * 测试rule与role绑定
     */
    @Test
    public void test_RELATIVE_Roles() {
        String url = "http://localhost:" + this.port + "/api/doctor/msg/role/relative/ruleId";
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("ruleId", 1)
                .params("roleIds", ImmutableList.of(11, 12, 13))
                .httpEntity();
        this.restTemplate.postForObject(url, httpEntity, Boolean.class);
    }

}
