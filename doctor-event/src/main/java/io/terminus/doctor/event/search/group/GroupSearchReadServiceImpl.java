package io.terminus.doctor.event.search.group;

import com.google.common.base.Throwables;
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
 * Desc: 猪群搜索服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Service
@Slf4j
public class GroupSearchReadServiceImpl implements GroupSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BaseGroupQueryBuilder baseGroupQueryBuilder;

    @Autowired
    private GroupSearchProperties groupSearchProperties;

    @Override
    public Response<Paging<SearchedGroup>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "groupCode");
            }
            // 构建查询条件, 并查询
            Criterias criterias = baseGroupQueryBuilder.buildCriterias(pageNo, pageSize, params);
            WithAggregations<SearchedGroup> searchedGroups = searcher.searchWithAggs(
                    groupSearchProperties.getIndexName(),
                    groupSearchProperties.getIndexType(),
                    template,
                    criterias,
                    SearchedGroup.class
            );
            Paging<SearchedGroup> paging = new Paging<>(searchedGroups.getTotal(), searchedGroups.getData());
            return Response.ok(paging);
        } catch (Exception e) {
            log.error("group search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.group.fail");
        }

    }
}
