package io.terminus.doctor.warehouse.service;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.warehouse.search.material.MaterialSearchReadService;
import io.terminus.doctor.warehouse.search.material.SearchedMaterial;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
public class DoctorMaterialSearchTest extends BasicServiceTest {

    @Autowired
    private MaterialSearchReadService materialSearchReadService;

    @Test
    public void testMaterialSearch() throws InterruptedException {
        try{
            String template = "search/search.mustache";
            Map<String, String> params = Maps.newHashMap();
            Paging<SearchedMaterial> paging = RespHelper.orServEx(materialSearchReadService.searchWithAggs(1, 5, template, params));
            System.out.println(paging.getTotal());
            System.out.println(paging.getData());
        } catch (Exception e) {
            System.out.println("搜索失败!");
        }
    }
}
