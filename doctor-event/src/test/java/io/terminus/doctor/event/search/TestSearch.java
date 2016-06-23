package io.terminus.doctor.event.search;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.search.group.GroupSearchReadService;
import io.terminus.doctor.event.search.group.SearchedGroup;
import io.terminus.doctor.event.search.pig.PigSearchReadService;
import io.terminus.doctor.event.search.pig.SearchedPig;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Desc: 搜索测试类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public class TestSearch extends BaseServiceTest {

    @Autowired
    private PigSearchReadService pigSearchReadService;

    @Autowired
    private GroupSearchReadService groupSearchReadService;

    @Test
    public void testPigSearch() throws InterruptedException {
        try{
            String template = "search/search.mustache";
            Map<String, String> params = Maps.newHashMap();
            // 前缀查询 替代 关键字
            params.put("q", "16");
            Paging<SearchedPig> paging = RespHelper.orServEx(pigSearchReadService.searchWithAggs(1, 200, template, params));
            System.out.println(paging.getTotal());
            // updatedAt 降序排列
            paging.getData().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("搜索失败!");
        }
    }

    @Test
    public void testGroupSearch() throws InterruptedException {
        try{
            String template = "search/search.mustache";
            Map<String, String> params = Maps.newHashMap();
            params.put("q", "保育");
            Paging<SearchedGroup> paging = RespHelper.orServEx(groupSearchReadService.searchWithAggs(1, 200, template, params));
            System.out.println(paging.getTotal());
            paging.getData().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("搜索失败!");
        }
    }
}
