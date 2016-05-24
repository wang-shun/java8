package io.terminus.doctor.event.search.pig;

import io.terminus.common.model.PageInfo;
import io.terminus.search.api.query.Aggs;
import io.terminus.search.api.query.Criterias;
import io.terminus.search.api.query.CriteriasBuilder;
import io.terminus.search.api.query.Highlight;
import io.terminus.search.api.query.Keyword;
import io.terminus.search.api.query.Range;
import io.terminus.search.api.query.Sort;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;

import java.util.List;
import java.util.Map;

/**
 * Desc: 公共查询条件创建类
 *      模板路径 /search/search.mustache
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
public abstract class BasePigQueryBuilder {

    public Criterias buildCriterias(Integer pageNo, Integer pageSize, Map<String, String> params) {

        CriteriasBuilder criteriasBuilder = new CriteriasBuilder();

        // 1. 分页 from 和 size 处理
        PageInfo pageInfo = new PageInfo(pageNo, pageSize);
        criteriasBuilder.withPageInfo(pageInfo.getOffset(), pageInfo.getLimit());

        // 2. 关键词 keyword 处理
        Keyword keyword = buildKeyword(params);
        criteriasBuilder.withKeyword(keyword);

        // 3. 构建单值 term 查询
        List<Term> termList = buildTerm(params);
        criteriasBuilder.withTerm(termList);

        // 4. 构建多值 terms 查询
        List<Terms> termsList = buildTerms(params);
        criteriasBuilder.withTerms(termsList);

        // 5. 构建范围 range 查询
        List<Range> ranges = buildRanges(params);
        criteriasBuilder.withRanges(ranges);

        // 6. 处理排序 sorts
        List<Sort> sorts = buildSort(params);
        criteriasBuilder.withSorts(sorts);

        // 7. 处理高亮 highlight
        List<Highlight> highlightList = buildHighlight(params);
        criteriasBuilder.withHighlights(highlightList);

        // 8. 构建聚合查询
        List<Aggs> aggsList = buildAggs(params);
        criteriasBuilder.withAggs(aggsList);

        return criteriasBuilder.build();
    }

    /**
     * 构建关键字查询 keyword
     * @param params    参数上下文
     * @return  关键字, 如果没有指定关键字, 返回null
     */
    protected abstract Keyword buildKeyword(Map<String, String> params);

    /**
     * 构建单值查询 term
     * @param params    参数上下文
     * @return  单值查询的列表, 如果没有指定的单值查询, 返回null或者空列表
     */
    protected abstract List<Term> buildTerm(Map<String, String> params);

    /**
     * 构建多值查询 terms
     *
     * @param params  参数上下文
     * @return  多值查询列表, 如果没有指定的多值查询, 返回null或者空列表
     */
    protected abstract List<Terms> buildTerms(Map<String, String> params);


    /**
     * 构建范围查询 ranges
     *
     * @param params  参数上下文
     * @return   范围查询列表, 如果没有指定的多值查询, 返回null或者空列表
     */
    protected abstract List<Range> buildRanges(Map<String, String> params);

    /**
     * 构建排序 sort
     *
     * @param params  参数上下文
     * @return  排序查询列表, 如果没有指定的多值查询, 返回null或者空列表
     */
    protected abstract List<Sort> buildSort(Map<String, String> params);

    /**
     * 构建高亮 highlight
     *
     * @param params  参数上下文
     * @return  需要高亮的字段列表, 如果没有需要高亮的字段, 返回null或者空列表
     */
    protected abstract List<Highlight> buildHighlight(Map<String, String> params);

    /**
     * 构建聚合查询  aggs
     *
     * 本方法假定上下文中含有名为aggs的参数, 如果没有这个参数, 表示不需要聚合
     *
     * 且值形式为 agg_name_1:field_name_1:agg_size_1$agg_name2:field_name2:agg_size2...agg_name_N:field_name_N:agg_size_N
     *
     * 即每个agg分为3部分, 首先是聚合的名称agg_name, 其次是对应的搜索字段名称field_name,最后是要返回相应聚合值的个数agg_size
     *
     * 然后每个agg用$来连接
     *
     * @param params  参数上下文
     * @return 聚合查询列表, 如果没有不需要聚合, 返回null或者空列表
     */
    protected abstract List<Aggs> buildAggs(Map<String, String> params);
}
