package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.search.group.SearchedGroupDto;
import io.terminus.doctor.event.search.pig.SearchedPigDto;
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
     * 查询母猪的状态
     * @see DoctorSearches#searchSowStatus(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_SowStatus() {
        String url = "/api/doctor/search/sowpigs/status";
        SearchedPigDto body = getForEntity(url, ImmutableMap.of("farmId", 12355), SearchedPigDto.class).getBody();
        System.out.println(body.getPigs().getTotal());
        System.out.println(body.getPigs().getData());
        System.out.println(body.getSowStatuses());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

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

    /**
     * 查询猪群的状态列表
     * @see DoctorSearches#searchGroupStatus(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_PigGroupsStatus() {
        String url = "/api/doctor/search/groups/status";
        SearchedGroupDto body = getForEntity(url, ImmutableMap.of("farmId", 12355), SearchedGroupDto.class).getBody();
        System.out.println(body.getGroups().getTotal());
        System.out.println(body.getGroups().getData());
        System.out.println(body.getAggPigTypes());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询所有猪舍
     * @see DoctorSearches#searchBarns(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_Barns() {
        String url = "/api/doctor/search/barns";
        Object body = getForEntity(url, ImmutableMap.of("farmId", 12355, "pigType", "3"), Object.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询所有的猪舍
     * @see DoctorSearches#searchAllBarns(Map)
     */
    @Test
    public void test_QUERY_AllBarns() {
        String url = "/api/doctor/search/barns/all";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355), List.class).getBody();
        System.out.println(body.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询所有的物料
     * @see DoctorSearches#searchAllMaterials(Map)
     */
    @Test
    public void test_QUERY_AllMaterials() {
        String url = "/api/doctor/search/materials/all";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355), List.class).getBody();
        System.out.println(body.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }
}
