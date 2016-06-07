package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import utils.HttpGetRequest;

import java.util.List;

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
        Assert.assertEquals(new Long(5), count);
    }

    /**
     * 分页获取系统消息
     */
    @Test
    public void test_PAGE_SysDoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/sys/messages")
                .params("pageNo", 1)
                .params("pageSize", 5)
                .build();
        Object object = this.restTemplate.getForObject(url, Object.class, ImmutableMap.of("port", this.port));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(object));
    }

    /**
     * 分页获取预警消息
     */
    @Test
    public void test_PAGE_WarnDoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/warn/messages")
                .params("pageNo", 1)
                .params("pageSize", 5)
                .build();
        Object object = this.restTemplate.getForObject(url, Object.class, ImmutableMap.of("port", this.port));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(object));
    }

    /**
     * 获取消息详情
     */
    @Test
    public void test_DETAIL_Message() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/message/detail")
                .params("id", 1)
                .build();
        DoctorMessage message = this.restTemplate.getForObject(url, DoctorMessage.class, ImmutableMap.of("port", this.port));
        System.out.println(message);
    }

    @Test
    public void test_SYS_listTemplates() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/sys/templates").build();
        List templates = this.restTemplate.getForObject(url, List.class, ImmutableMap.of("port", this.port));
        System.out.println(templates.size() + "----" + templates);
    }

    @Test
    public void test_WARN_listTemplates() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/warn/templates").build();
        List templates = this.restTemplate.getForObject(url, List.class, ImmutableMap.of("port", this.port));
        System.out.println(templates.size() + "----" + templates);
    }

    @Test
    public void test_DELETE_Template() {
        // 查询
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/template/detail")
                .params("id", 1)
                .build();
        DoctorMessageRuleTemplate template = this.restTemplate.getForObject(url, DoctorMessageRuleTemplate.class, ImmutableMap.of("port", this.port));
        System.out.println(template);
        // 删除
        url = "http://localhost:"+ this.port +"/api/doctor/msg/template?id=1";
        this.restTemplate.delete(url);

        // 查询
        url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/template/detail")
                .params("id", 1)
                .build();
        template = this.restTemplate.getForObject(url, DoctorMessageRuleTemplate.class, ImmutableMap.of("port", this.port));
        System.out.println(template);
    }
}
