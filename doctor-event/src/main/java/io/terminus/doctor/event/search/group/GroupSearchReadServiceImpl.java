package io.terminus.doctor.event.search.group;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.event.search.barn.SearchedBarnDto;
import io.terminus.doctor.event.search.query.GroupPaging;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.Pagination;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import io.terminus.search.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 猪群搜索服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Service
@Slf4j
@RpcProvider
public class GroupSearchReadServiceImpl implements GroupSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BaseGroupQueryBuilder baseGroupQueryBuilder;

    @Autowired
    private GroupSearchProperties groupSearchProperties;

    @Override
    public Response<SearchedGroupDto> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "groupCode");

                // 关键字小写处理
                params.put("q", q.toLowerCase());
            }
            // 1. 猪群类型聚合处理, ... 其他
            String aggs = params.get("aggs");
            if (StringUtils.isBlank(aggs)) {
                params.put("aggs", "aggs_pigType:pigType:0"); // id:field:size
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
            GroupPaging<SearchedGroup> paging = new GroupPaging<>(searchedGroups.getTotal(), searchedGroups.getData());

            // 获取聚合后的数据
            List<SearchedBarnDto.PigType> aggPigTypes = Lists.newArrayList();
            Map<String, List<Bucket>> aggregations = searchedGroups.getAggregations();
            if (aggregations != null) {
                // 猪群类型
                aggregations.get("aggs_pigType").forEach(bucket ->
                        aggPigTypes.add(SearchedBarnDto.createPigType(PigType.from(Integer.parseInt(bucket.getKey())), bucket.getDoc_count())));
            }

            //set一发每次查询结果的猪群里猪的数量
            paging.setCount(getGroupQty(template, params));

            SearchedGroupDto searchedGroupDto = SearchedGroupDto.builder()
                    .groups(paging)
                    .aggPigTypes(aggPigTypes)
                    .build();
            return Response.ok(searchedGroupDto);
        } catch (Exception e) {
            log.error("group search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.group.fail");
        }

    }

    private Long getGroupQty(String template, Map<String, String> params) {
        params.remove("aggs");
        Criterias criterias = baseGroupQueryBuilder.buildCriterias(0, Integer.MAX_VALUE, params);
        Pagination<SearchedGroup> searchedGroups = searcher.search(
                groupSearchProperties.getIndexName(),
                groupSearchProperties.getIndexType(),
                template,
                criterias,
                SearchedGroup.class
        );
        return CountUtil.sumInt(searchedGroups.getData(), SearchedGroup::getQuantity);
    }
}
