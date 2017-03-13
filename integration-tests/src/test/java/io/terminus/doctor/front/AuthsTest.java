package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontPrimaryWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.parana.auth.model.CompiledTree;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc: 权限树
 * Mail: houly@terminus.io
 * Data: 下午3:21 16/6/7
 * Author: houly
 */
@SpringApplicationConfiguration(value = {FrontPrimaryWebConfiguration.class})
public class AuthsTest extends BaseFrontWebTest{
    @Test
    public void getTree(){
        ResponseEntity<CompiledTree> result= restTemplate.getForEntity("http://localhost:{port}/api/auth/tree?role={role}",  CompiledTree.class, ImmutableMap.of("port", port, "role", "SUB"));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), notNullValue());
        System.out.println(JsonMapperUtil.JSON_NON_EMPTY_MAPPER.toJson(result.getBody()));
    }
}
