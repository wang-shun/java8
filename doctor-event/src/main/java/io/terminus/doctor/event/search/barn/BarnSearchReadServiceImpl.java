package io.terminus.doctor.event.search.barn;

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
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class BarnSearchReadServiceImpl implements BarnSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BaseBarnQueryBuilder baseBarnQueryBuilder;

    @Autowired
    private BarnSearchProperties barnSearchProperties;

    @Override
    public Response<Paging<SearchedBarn>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        // 获取关键词, 设置高亮
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            params.put("highlight", "pigCode_batchNo");
        }
        // 构建查询条件, 并查询
        Criterias criterias = baseBarnQueryBuilder.buildCriterias(pageNo, pageSize, params);
        WithAggregations<SearchedBarn> searchedBarns = searcher.searchWithAggs(
                barnSearchProperties.getIndexName(),
                barnSearchProperties.getIndexType(),
                template,
                criterias,
                SearchedBarn.class
        );
        Paging<SearchedBarn> paging = new Paging<>(searchedBarns.getTotal(), searchedBarns.getData());
        return Response.ok(paging);
    }
}
