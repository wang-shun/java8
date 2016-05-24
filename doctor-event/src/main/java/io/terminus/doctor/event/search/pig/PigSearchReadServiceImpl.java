package io.terminus.doctor.event.search.pig;

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
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Service
@Slf4j
public class PigSearchReadServiceImpl implements PigSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BasePigQueryBuilder basePigQueryBuilder;

    @Autowired
    private PigSearchProperties pigSearchProperties;

    @Override
    public Response<Paging<SearchedPig>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        // 获取关键词, 设置高亮
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            params.put("highlight", "pigCode");
        }
        // 构建查询条件, 并查询
        Criterias criterias = basePigQueryBuilder.buildCriterias(pageNo, pageSize, params);
        WithAggregations<SearchedPig> searchedPigs = searcher.searchWithAggs(
                pigSearchProperties.getIndexName(),
                pigSearchProperties.getIndexType(),
                template,
                criterias,
                SearchedPig.class
        );
        Paging<SearchedPig> paging = new Paging<>(searchedPigs.getTotal(), searchedPigs.getData());
        return Response.ok(paging);
    }
}
