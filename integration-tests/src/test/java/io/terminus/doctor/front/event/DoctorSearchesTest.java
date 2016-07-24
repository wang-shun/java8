package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.search.group.SearchedGroupDto;
import io.terminus.doctor.event.search.pig.SearchedPigDto;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.component.DoctorSearches;
import io.terminus.doctor.web.front.event.controller.DoctorPigs;
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
     * 查询母猪suggest
     * @see DoctorSearches#searchSowsSuggest(Integer, Map)
     */
    @Test
    public void test_QUERY_SowSuggest() {
        String url = "/api/doctor/search/sowpigs/suggest";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355, "size", 2, "q", "lY1"), List.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询多状态的母猪
     * @see DoctorSearches#searchSowPigs(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_SowStatuses() {
        String url = "/api/doctor/search/sowpigs";
        Paging paging = getForEntity(url, ImmutableMap.of("farmId", 12355, "statuses", "1_3_4"), Paging.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(paging.getData()));
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
     * 查询猪群
     * @see DoctorSearches#searchGroups(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_PigGroups() {
        String url = "/api/doctor/search/groups";
        Paging body = getForEntity(url, ImmutableMap.of("farmId", 12355, "q", "yy"), Paging.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询多状态的猪群
     * @see DoctorSearches#searchGroups(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_GroupStatuses() {
        String url = "/api/doctor/search/groups";
        Paging body = getForEntity(url, ImmutableMap.of("farmId", 12355, "pigTypes", "1_2"), Paging.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询所有猪舍
     * @see DoctorSearches#searchBarns(Integer, Integer, Map)
     */
    @Test
    public void test_QUERY_Barns() {
        String url = "/api/doctor/search/barns";
        // Object body = getForEntity(url, ImmutableMap.of("farmId", 12355, "pigType", "3"), Object.class).getBody();
        Object body = getForEntity(url, ImmutableMap.of("farmId", 12355, "pigTypes", "1_2"), Object.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    @Test
    public void test_QUERY_BarnsByKeyWord() {
        String url = "/api/doctor/search/barns";
        Object body = getForEntity(url, ImmutableMap.of("farmId", 12355, "q", "2"), Object.class).getBody();
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

    /**
     * 查询所有的物料
     * @see DoctorSearches#searchAllMaterials(Map)
     */
    @Test
    public void test_QUERY_AllMaterialsWithNotIds() {
        String url = "/api/doctor/search/materials/all";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355, "exIds", "19,14"), List.class).getBody();
        System.out.println(body.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询所有的物料
     * @see DoctorSearches#searchAllMaterials(Map)
     */
    @Test
    public void test_QUERY_AllMaterialsWithKeyWord() {
        String url = "/api/doctor/search/materials/all";
//        String q = "物料测试19";
        String q = "物料测试material";
        List body = getForEntity(url, ImmutableMap.of("q", q,"farmId", 12355), List.class).getBody();
        System.out.println(body.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 查询物料
     * @see DoctorSearches#searchSuggestMaterials(Integer, Map)
     */
    @Test
    public void test_QUERY_SuggestMaterialsWithKeyWord() {
        String url = "/api/doctor/search/materials/suggest";
        String q = "物料测试ma";
        List body = getForEntity(url, ImmutableMap.of("farmId", 12355, "size", 2 ,"q", q), List.class).getBody();
        System.out.println(body.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }

    /**
     * 获取猪只提示的消息
     * @see DoctorPigs#queryPigNotifyMessages(Long)
     */
    @Test
    public void test_QUERY_PigNotifyMessage() {
        String url = "/api/doctor/pigs/notify/message";
        List messages = getForEntity(url, ImmutableMap.of("pigId", "144"), List.class).getBody();
        System.out.println(messages.size());
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(messages));
    }

    @Test
    public void test_Report() {
        String url = "/api/doctor/report/daily";
        Object body = getForEntity(url, ImmutableMap.of("farmId", 12355, "date", "2016-07-25"), Object.class).getBody();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(body));
    }
}
