package io.terminus.doctor.front.msg;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.msg.model.DoctorMessage;
import org.junit.Assert;
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
}
