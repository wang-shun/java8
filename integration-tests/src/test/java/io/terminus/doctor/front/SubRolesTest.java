package io.terminus.doctor.front;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontPrimaryWebConfiguration;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.web.front.role.Sub;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Desc: 子账号角色相关测试
 * Mail: houly@terminus.io
 * Data: 下午1:37 16/6/7
 * Author: houly
 */
@SpringApplicationConfiguration(value = {FrontPrimaryWebConfiguration.class})
public class SubRolesTest extends BaseFrontWebTest{

    @Test
    public void findAllRoles(){
        ResponseEntity<List> result= restTemplate.getForEntity("http://localhost:{port}/api/sub/role/all",  List.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
    }

    @Test
    public void pagination(){
        ResponseEntity<Paging> result= restTemplate.getForEntity("http://localhost:{port}/api/sub/role/pagination",  Paging.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getTotal(), not(0));
        System.out.println(JsonMapperUtil.JSON_NON_EMPTY_MAPPER.toJson(result.getBody()));
    }

    @Test
    public void createRole(){
        SubRole subRole = mockSubRole();
        ResponseEntity<Long> result = restTemplate.postForEntity("http://localhost:{port}/api/sub/role", subRole,  Long.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(2L));
    }

    @Test
    public void updateRole(){
        SubRole subRole = mockSubRole();
        subRole.setId(1L);
        restTemplate.put("http://localhost:{port}/api/sub/role", subRole, ImmutableMap.of("port", port));

        ResponseEntity<Paging> result2= restTemplate.getForEntity("http://localhost:{port}/api/sub/role/pagination",  Paging.class, ImmutableMap.of("port", port));
        assertThat(result2.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void findByIdForUpdate(){
        ResponseEntity<SubRole> result= restTemplate.getForEntity("http://localhost:{port}/api/sub/role/1",  SubRole.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), notNullValue());
        System.out.println(JsonMapperUtil.JSON_NON_EMPTY_MAPPER.toJson(result.getBody()));
    }
    private SubRole mockSubRole(){
        SubRole subRole = new SubRole();
        subRole.setName("测试角色名称");
        subRole.setAppKey("MOBILE");
        subRole.setDesc("测试描述");
        subRole.setAllowJson("[\"manage_back_category\"]");
        System.out.println(JsonMapperUtil.nonEmptyMapper().toJson(subRole));
        return subRole;
    }
}
