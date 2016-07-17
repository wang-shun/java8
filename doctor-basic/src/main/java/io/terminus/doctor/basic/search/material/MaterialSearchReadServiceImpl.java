package io.terminus.doctor.basic.search.material;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Desc: 物料ElasticSearch搜索写接口
 * Mail: chk@terminus.io
 * author: IceMimosa
 * Date: 16/6/16
 */
@Slf4j
@Service
@RpcProvider
public class MaterialSearchReadServiceImpl implements MaterialSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BaseMaterialQueryBuilder baseMaterialQueryBuilder;

    @Autowired
    private MaterialSearchProperties materialSearchProperties;

    @Override
    public Response<Paging<SearchedMaterial>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "materialName");
            }
            // 构建查询条件, 并查询
            Criterias criterias = baseMaterialQueryBuilder.buildCriterias(pageNo, pageSize, params);
            WithAggregations<SearchedMaterial> searchedBarns = searcher.searchWithAggs(
                    materialSearchProperties.getIndexName(),
                    materialSearchProperties.getIndexType(),
                    template,
                    criterias,
                    SearchedMaterial.class
            );
            Paging<SearchedMaterial> paging = new Paging<>(searchedBarns.getTotal(), searchedBarns.getData());
            return Response.ok(paging);
        } catch (Exception e) {
            log.error("material search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.material.fail");
        }

    }
}
