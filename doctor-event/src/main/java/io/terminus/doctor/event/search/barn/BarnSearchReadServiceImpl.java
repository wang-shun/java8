package io.terminus.doctor.event.search.barn;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import io.terminus.search.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
@RpcProvider
public class BarnSearchReadServiceImpl implements BarnSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BaseBarnQueryBuilder baseBarnQueryBuilder;

    @Autowired
    private BarnSearchProperties barnSearchProperties;

    @Override
    public Response<SearchedBarnDto> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "name");

                // 搜索词做小写处理
                params.put("q", q.toLowerCase());
            }

            // 1. 猪群类型聚合处理, ... 其他
            String aggs = params.get("aggs");
            if (StringUtils.isBlank(aggs)) {
                params.put("aggs", "aggs_pigType:pigType:0"); // id:field:size
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
            searchedBarns.getData().forEach(barn -> { // 处理猪舍详情类型
                //分娩舍特殊处理
                if (Objects.equals(barn.getPigType(), PigType.DELIVER_SOW.getValue())) {
                    barn.setType(PigSearchType.SOW_GROUP.getValue());
                } else if (PigType.isSow(barn.getPigType())) {
                    barn.setType(PigSearchType.SOW.getValue());
                } else if (PigType.isBoar(barn.getPigType())) {
                    barn.setType(PigSearchType.BOAR.getValue());
                } else if (PigType.isGroup(barn.getPigType())) {
                    barn.setType(PigSearchType.GROUP.getValue());
                }
            });
            Paging<SearchedBarn> paging = new Paging<>(searchedBarns.getTotal(), searchedBarns.getData());

            // 获取聚合后的数据
            List<SearchedBarnDto.PigType> aggPigTypes = Lists.newArrayList();
            Map<String, List<Bucket>> aggregations = searchedBarns.getAggregations();
            if (aggregations != null) {
                // 猪群类型
                aggregations.get("aggs_pigType").forEach(bucket ->
                        aggPigTypes.add(SearchedBarnDto.createPigType(
                                PigType.from(Integer.parseInt(bucket.getKey())))));
            }

            // 构建返回对象
            SearchedBarnDto searchedBarnDto = SearchedBarnDto.builder()
                    .barns(paging)
                    .aggPigTypes(aggPigTypes)
                    .build();
            return Response.ok(searchedBarnDto);
        } catch (Exception e) {
            log.error("barn search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.barn.fail");
        }

    }
}
