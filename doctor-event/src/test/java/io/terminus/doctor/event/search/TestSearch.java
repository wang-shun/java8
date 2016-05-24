package io.terminus.doctor.event.search;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
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
    
    @Test
    public void testPigSearch() throws InterruptedException {
        String template = "search/search.mustache";
        Map<String, String> params = Maps.newHashMap();
        Paging<SearchedPig> paging = RespHelper.orServEx(pigSearchReadService.searchWithAggs(1, 2, template, params));
        System.out.println(paging.getTotal());
        System.out.println(paging.getData());
    }
}
