package io.terminus.doctor.event.search.pig;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.search.barn.BarnSearchESInitiator;
import io.terminus.doctor.event.search.barn.SearchedBarn;
import io.terminus.doctor.event.search.group.GroupSearchESInitiator;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.search.api.Searcher;
import io.terminus.search.api.model.WithAggregations;
import io.terminus.search.api.query.Criterias;
import io.terminus.search.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪(索引对象)查询服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Service
@Slf4j
@RpcProvider
public class PigSearchReadServiceImpl implements PigSearchReadService {

    @Autowired
    private Searcher searcher;

    @Autowired
    private BasePigQueryBuilder basePigQueryBuilder;

    @Autowired
    private PigSearchProperties pigSearchProperties;

    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Autowired
    private PigSearchESInitiator pigSearchESInitiator;
    @Autowired
    private GroupSearchESInitiator groupSearchESInitiator;
    @Autowired
    private BarnSearchESInitiator barnSearchESInitiator;

    @Override
    public Response<SearchedPigDto> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params) {
        try{
            // 获取关键词, 设置高亮
            String q = params.get("q");
            if (StringUtils.isNotBlank(q)) {
                // 暂不做高亮处理
                // params.put("highlight", "pigCode");

                // 关键字 转化为 小写
                params.put("q", q.toLowerCase());
            }

            // 获取存在的猪状态
            String aggs = params.get("aggs");
            if (StringUtils.isBlank(aggs)) {
                params.put("aggs", "aggs_sowStatus:status:0");
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
                // 如果是待分娩状态, 获取妊娠检查的时间
                if (Objects.equals(searchedPig.getStatus(), PigStatus.Farrow.getKey())) {
                    DoctorPigTrack pigTrack = RespHelper.orServEx(
                            doctorPigReadService.findPigTrackByPigId(searchedPig.getId()));
                    if (pigTrack != null && StringUtils.isNotBlank(pigTrack.getExtra())) {
                        setCheckDate(searchedPig, pigTrack);
                    }
                }

                // 处理 KongHuaiPregCheckResult
                if (searchedPig.getStatus() == PigStatus.KongHuai.getKey() && searchedPig.getPregCheckResult() != null) {
                    searchedPig.setStatus(searchedPig.getPregCheckResult());
                }
            });
            Paging<SearchedPig> paging = new Paging<>(searchedPigs.getTotal(), searchedPigs.getData());

            // 处理聚合属性
            List<SearchedPigDto.SowStatus> sowStatuses = Lists.newArrayList();
            Map<String, List<Bucket>> aggregations = searchedPigs.getAggregations();
            if (aggregations != null) {
                // 母猪状态
                aggregations.get("aggs_sowStatus").forEach(bucket ->
                        sowStatuses.add(SearchedPigDto.createStatus(
                                PigStatus.from(Integer.parseInt(bucket.getKey())))));
            }

            // 构建返回对象
            SearchedPigDto searchedPigDto = SearchedPigDto.builder()
                    .pigs(paging)
                    .sowStatuses(sowStatuses)
                    .build();

            return Response.ok(searchedPigDto);
        } catch (Exception e) {
            log.error("pig search failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("search.pig.fail");
        }
    }

    /**
     * 获取妊娠检查的时间
     * @return
     */
    private void setCheckDate(SearchedPig searchedPig, DoctorPigTrack pigTrack) {
        Map map = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(pigTrack.getExtra(), Map.class);
        String key = "checkDate";
        if (map != null && map.get(key) != null) {
            searchedPig.getExtra().put(key, new Date((long)map.get(key)));
        }
    }

    public Response<Boolean> initIndex(Integer type){
        try {
            switch (type) {
                case 0:
                    pigSearchESInitiator.init();
                    groupSearchESInitiator.init();
                    barnSearchESInitiator.init();
                    break;
                case 1:
                    pigSearchESInitiator.init();
                    break;
                case 2:
                    groupSearchESInitiator.init();
                    break;
                case 3:
                    barnSearchESInitiator.init();
                    break;
                default:
                    break;
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("init index failed , cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("init.index.failed");
        }
    }

    @Override
    public Response<List<SearchedBarn.BarnStatus>> searchBarnStatusByBarnId(Long barnId, String template) {
        try {
            PageInfo pageInfo = PageInfo.of(null, null);
            ImmutableMap<String, String> params = ImmutableMap.of("aggs", "aggs_sowStatus:status:0", "barnId", String.valueOf(barnId));

            Criterias criterias = basePigQueryBuilder.buildCriterias(pageInfo.getOffset(), pageInfo.getLimit(), params);
            WithAggregations<SearchedPig> searchedPigs = searcher.searchWithAggs(
                    pigSearchProperties.getIndexName(),
                    pigSearchProperties.getIndexType(),
                    template,
                    criterias,
                    SearchedPig.class
            );

            // 处理聚合属性
            Map<String, List<Bucket>> aggregations = searchedPigs.getAggregations();
            if (aggregations == null || !notEmpty(aggregations.get("aggs_sowStatus"))) {
                return Response.ok(Collections.emptyList());
            }

            // 母猪状态
            List<SearchedBarn.BarnStatus> barnStatuses = aggregations.get("aggs_sowStatus").stream()
                    .map(bucket -> SearchedBarn.createPigStatus(
                            PigStatus.from(Integer.parseInt(bucket.getKey())), bucket.getDoc_count().intValue()))
                    .collect(Collectors.toList());
            return Response.ok(barnStatuses);
        } catch (Exception e) {
            log.error("pig search failed, barnId:{}, template:{} cause by {}", barnId, template, Throwables.getStackTraceAsString(e));
            return Response.fail("search.pig.fail");
        }
    }
}
