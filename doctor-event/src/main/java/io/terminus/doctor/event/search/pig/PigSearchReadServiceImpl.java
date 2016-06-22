package io.terminus.doctor.event.search.pig;

import com.google.common.base.Throwables;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Desc: 猪(索引对象)查询服务
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
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "pigCode");
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
            // 处理日龄
            searchedPigs.getData().forEach(searchedPig -> {
                if (searchedPig.getBirthDate() != null) {
                    searchedPig.setDayAge((int)(DateTime.now()
                            .minus(searchedPig.getBirthDate().getTime()).getMillis() / (1000 * 60 * 60 * 24) + 1));
                }
            });
            Paging<SearchedPig> paging = new Paging<>(searchedPigs.getTotal(), searchedPigs.getData());
            return Response.ok(paging);
        } catch (Exception e) {
            log.error("pig search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.pig.fail");
        }
    }
}
