package io.terminus.doctor.event.search.barn;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.search.pig.PigSearchReadService;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import io.terminus.search.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Autowired
    private PigSearchReadService pigSearchReadService;

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

            // 处理猪舍详情类型
            searchedBarns.getData().forEach(barn -> barn.setType(getArgBarnType(barn.getPigType())));

            Paging<SearchedBarn> paging = new Paging<>(searchedBarns.getTotal(), searchedBarns.getData());

            // 获取聚合后的数据
            List<SearchedBarnDto.PigType> aggPigTypes = Lists.newArrayList();
            Map<String, List<Bucket>> aggregations = searchedBarns.getAggregations();
            if (aggregations != null) {
                // 猪群类型
                aggregations.get("aggs_pigType").forEach(bucket ->
                        aggPigTypes.add(SearchedBarnDto.createPigType(PigType.from(Integer.parseInt(bucket.getKey())), bucket.getDoc_count())));
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

    @Override
    public Response<Paging<SearchedBarn>> searchTypeWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
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

            // 处理猪舍详情类型
            searchedBarns.getData().forEach(barn -> {
                barn.setType(getArgBarnType(barn.getPigType()));
                barn.setBarnStatuses(getBarnStatus(barn, template));
            });

            return Response.ok(new Paging<>(searchedBarns.getTotal(), searchedBarns.getData()));
        } catch (Exception e) {
            log.error("barn search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.barn.fail");
        }
    }

    private static Integer getArgBarnType(Integer barnType) {
        if (Objects.equals(barnType, PigType.DELIVER_SOW.getValue())) {
            return PigSearchType.SOW_GROUP.getValue();
        } else if (PigType.isSow(barnType)) {
            return PigSearchType.SOW.getValue();
        } else if (PigType.isBoar(barnType)) {
            return PigSearchType.BOAR.getValue();
        } else if (PigType.isGroup(barnType)) {
            return PigSearchType.GROUP.getValue();
        } else {
            return barnType;
        }
    }

    private List<SearchedBarn.BarnStatus> getBarnStatus(SearchedBarn barn, String template) {
        //猪群的情况：直接返回猪舍里的猪群的数量
        if (Objects.equals(barn.getType(), PigSearchType.GROUP.getValue())) {
            if (barn.getPigGroupCount() <= 0) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(SearchedBarn.createGroupStatus(PigType.from(barn.getPigType()), barn.getPigGroupCount()));
        }

        //产房猪舍的情况：有产房仔猪和母猪
        if (Objects.equals(barn.getType(), PigSearchType.SOW_GROUP.getValue())) {
            List<SearchedBarn.BarnStatus> barnStatuses = Lists.newArrayList();
            if (barn.getPigGroupCount() > 0) {
                barnStatuses.add(SearchedBarn.createFarrowStatus(barn.getPigGroupCount()));
            }
            if (barn.getPigCount() > 0) {
                barnStatuses.addAll(RespHelper.orServEx(pigSearchReadService.searchBarnStatusByBarnId(barn.getId(), template)));
            }
            return barnStatuses;
        }

        //母猪和公猪舍：按照状态聚合
        if (barn.getPigCount() > 0) {
            return RespHelper.orServEx(pigSearchReadService.searchBarnStatusByBarnId(barn.getId(), template));
        }
        return Collections.emptyList();
    }


}
