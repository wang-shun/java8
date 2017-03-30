package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.web.front.msg.controller.DoctorMessages;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import io.terminus.doctor.utils.HttpGetRequest;
import io.terminus.doctor.utils.HttpPostRequest;

import java.util.List;
import java.util.Map;

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
     * @see DoctorMessages#findNoReadCount()
     */
    @Test
    public void test_NO_READ_DoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/noReadCount").build();
        Long count = this.restTemplate.getForObject(url, Long.class, ImmutableMap.of("port", this.port));
        Assert.assertEquals(new Long(2), count);
    }

    /**
     * 分页获取系统消息
     * @see DoctorMessages#pagingSysDoctorMessages(Integer, Integer, Map)
     */
    @Test
    public void test_PAGE_SysDoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/sys/messages")
                .params("pageNo", 1)
                .params("pageSize", 5)
                .build();
        Object object = this.restTemplate.getForObject(url, Object.class, ImmutableMap.of("port", this.port));
        System.out.println(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(object));
    }

    /**
     * 分页获取预警消息
     * @see DoctorMessages#pagingWarnDoctorMessages(Integer, Integer, Map)
     */
    @Test
    public void test_PAGE_WarnDoctorMessages() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/warn/messages")
                .params("pageNo", 1)
                .params("pageSize", 5)
                .build();
        Object object = this.restTemplate.getForObject(url, Object.class, ImmutableMap.of("port", this.port));
        System.out.println(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(object));
    }

    /**
     * 获取消息详情
     * @see DoctorMessages#findMessageDetail(Long)
     */
    @Test
    public void test_DETAIL_Message() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/message/detail")
                .params("id", 1)
                .build();
        DoctorMessage message = this.restTemplate.getForObject(url, DoctorMessage.class, ImmutableMap.of("port", this.port));
        System.out.println(message);
    }

    /**
     * 获取系统消息模板
     * @see DoctorMessages#listSysTemplate(Map)
     */
    @Test
    public void test_SYS_listTemplates() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/sys/templates").build();
        List templates = this.restTemplate.getForObject(url, List.class, ImmutableMap.of("port", this.port));
        System.out.println(templates.size() + "----" + templates);
    }

    /**
     * 获取预警消息模板
     * @see DoctorMessages#listWarnTemplate(Map)
     */
    @Test
    public void test_WARN_listTemplates() {
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/warn/templates").build();
        List templates = this.restTemplate.getForObject(url, List.class, ImmutableMap.of("port", this.port));
        System.out.println(templates.size() + "----" + templates);
    }

    /**
     * 删除消息模板
     * @see DoctorMessages#deleteTemplate(Long)
     * @see DoctorMessages#getTemplateById(Long)
     */
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

    /**
     * 更新模板信息
     * @see DoctorMessages#createOrUpdateTemplate(DoctorMessageRuleTemplate)
     */
    @Test
    public void test_UPDATE_Template() {
        // 查询
        String url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/template/detail")
                .params("id", 1)
                .build();
        DoctorMessageRuleTemplate template = this.restTemplate.getForObject(url, DoctorMessageRuleTemplate.class, ImmutableMap.of("port", this.port));
        template.setName("测试测试11111修改了");
        // 更新
        url = "http://localhost:"+ this.port +"/api/doctor/msg/template";
        this.restTemplate.postForObject(url,
                HttpPostRequest.bodyRequest().params(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(template)),
                Object.class);

        // 查询
        url = HttpGetRequest.url("http://localhost:{port}/api/doctor/msg/template/detail")
                .params("id", 1)
                .build();
        template = this.restTemplate.getForObject(url, DoctorMessageRuleTemplate.class, ImmutableMap.of("port", this.port));
        System.out.println(template);
    }
}
