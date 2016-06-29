package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.component.DoctorSearches;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Desc: 搜索测试类
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/27
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorSearchesTest extends BaseFrontWebTest {

    /**
     * 查询所有的公猪
     * @see DoctorSearches#searchAllBoarPigs(Map)
     */
    @Test
    public void test_All_BoarPigs() {
        String url = "/api/doctor/search/boarpigs/all";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355), List.class).getBody();
        System.out.println(body.size());
        System.out.println(body);
    }

    @Test
    public void test_QUERY_Barns() {
        String url = "/api/doctor/search/barns";
        Object body = getForEntity(url, ImmutableMap.of("farmId", 12355), Object.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }
}
