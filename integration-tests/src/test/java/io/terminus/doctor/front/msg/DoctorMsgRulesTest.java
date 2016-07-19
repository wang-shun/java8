package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableList;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.utils.HttpGetRequest;
import io.terminus.doctor.utils.HttpPostRequest;
import io.terminus.doctor.web.front.msg.controller.DoctorMsgRules;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;

import java.util.List;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/8
 */
@SpringApplicationConfiguration(value = {FrontWebConfiguration.class})
public class DoctorMsgRulesTest extends BaseFrontWebTest {

    /**
     * 根据猪场id获取规则列表
     * @see DoctorMsgRules#listRulesByFarmId(Long)
     */
    @Test
    public void test_QUERY_RuleByFarmId() {
        String url = HttpGetRequest
                .url("http://localhost:" + this.port + "/api/doctor/msg/rule/farmId")
                .params("farmId", 1)
                .build();
        List list = (List)this.restTemplate.getForObject(url, Object.class);
        System.out.println(list.size());
    }

    /**
     * 查询角色与猪场规则绑定
     * @see DoctorMsgRules#findHasCheckedRule(Long, Long)
     */
    @Test
    public void test_QUERY_HasCheckRuls() {
        // 绑定规则 (多个 模板 id)
        String url = "http://localhost:" + this.port + "/api/doctor/msg/role/relative/roleId";
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("roleId", 1)
                .params("ruleIds", ImmutableList.of(1, 2, 3))
                .httpEntity();
        this.restTemplate.postForObject(url, httpEntity, Boolean.class);

        // 查询
        url = HttpGetRequest
                .url("http://localhost:" + this.port + "/api/doctor/msg/rule/hasFlag")
                .params("roleId", 1)
                .params("farmId", 1)
                .build();
        Object ruleRoles = this.restTemplate.getForObject(url, Object.class);
        System.out.println(ruleRoles);
    }

    /**
     * 根据规格id获取规则详情
     * @see DoctorMsgRules#findDetailById(Long)
     */
    @Test
    public void test_DETAIL_RuleByFarmId() {
        String url = HttpGetRequest
                .url("http://localhost:" + this.port + "/api/doctor/msg/rule/detail")
                .params("id", 1)
                .build();
        DoctorMessageRule rule = this.restTemplate.getForObject(url, DoctorMessageRule.class);
        System.out.println(rule);
    }

    /**
     * 测试rule与role绑定
     * @see DoctorMsgRules#relateRuleRolesByRuleId(Long, List)
     * @see DoctorMsgRules#findRolesByRuleId(Long)
     */
    @Test
    public void test_RELATIVE_Roles() {
        // 绑定规则 (多个 角色 id)
        String url = "http://localhost:" + this.port + "/api/doctor/msg/role/relative/ruleId";
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("ruleId", 1)
                .params("roleIds", ImmutableList.of(11, 12, 13))
                .httpEntity();
        this.restTemplate.postForObject(url, httpEntity, Boolean.class);

        // 查询
        url = HttpGetRequest
                .url("http://localhost:" + this.port + "/api/doctor/msg/role/ruleId")
                .params("ruleId", 1)
                .build();
        List ruleRoles = (List)this.restTemplate.getForObject(url, Object.class);
        System.out.println(ruleRoles.size());
    }

    /**
     * 测试rule与role绑定
     * @see DoctorMsgRules#relateRuleRolesByRoleId(Long, List)
     * @see DoctorMsgRules#findRolesByRoleId(Long)
     */
    @Test
    public void test_RELATIVE_Rules() {
        // 绑定规则 (多个 模板 id)
        String url = "http://localhost:" + this.port + "/api/doctor/msg/role/relative/roleId";
        HttpEntity httpEntity = HttpPostRequest.formRequest()
                .param("roleId", 1)
                .params("ruleIds", ImmutableList.of(1, 2))
                .httpEntity();
        this.restTemplate.postForObject(url, httpEntity, Boolean.class);

        // 查询
        url = HttpGetRequest
                .url("http://localhost:" + this.port + "/api/doctor/msg/role/roleId")
                .params("roleId", 1)
                .build();
        List ruleRoles = (List)this.restTemplate.getForObject(url, Object.class);
        System.out.println(ruleRoles.size());
    }
}
