package io.terminus.doctor.event.search.group;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.search.api.query.Aggs;
import io.terminus.search.api.query.Highlight;
import io.terminus.search.api.query.Keyword;
import io.terminus.search.api.query.Range;
import io.terminus.search.api.query.Sort;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Desc: 默认猪群(索引对象)查询条件构建
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
public class DefaultGroupQueryBuilder extends BaseGroupQueryBuilder {

    @Override
    protected Keyword buildKeyword(Map<String, String> params) {
        // 搜索关键词 处理
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            return new Keyword(ImmutableList.of("groupCode", "batchNo"), q);
        }
        return null;
    }

    @Override
    protected List<Term> buildTerm(Map<String, String> params) {
        List<Term> termList = Lists.newArrayList();
        // 1. 公司id
        term(termList, params, "orgId");
        // 2. 猪场id
        term(termList, params, "farmId");
        // 3. 猪群类型
        term(termList, params, "pigType");
        term(termList, params, "sex");
        // 4. 品种
        term(termList, params, "breedId");
        // 5. 品系
        term(termList, params, "geneticId");
        // 6. 状态
        term(termList, params, "status");
        return termList;
    }
    private void term(List<Term> termList, Map<String, String> params, String key) {
        String value = params.get(key);
        if (StringUtils.isNotBlank(value)) {
            termList.add(new Term(key, value));
        }
    }

    @Override
    protected List<Terms> buildTerms(Map<String, String> params) {
        List<Terms> termsList = Lists.newArrayList();
        // 1. 多状态
        String statuses = params.get("statuses");
        if (StringUtils.isNotBlank(statuses)) {
            List<String> statusList = Splitters.UNDERSCORE.splitToList(statuses);
            termsList.add(new Terms("statuses", statusList));
        }
        return termsList;
    }

    @Override
    protected List<Range> buildRanges(Map<String, String> params) {
        List<Range> ranges = Lists.newArrayList();
        // 1. 建群日期范围
        String open_f = params.get("open_f");
        String open_t = params.get("open_t");
        if (StringUtils.isNotBlank(open_f) || StringUtils.isNotBlank(open_t)) {
            ranges.add(new Range("openAt", open_f, open_t));
        }
        // 2. 日龄
        String avgday_f = params.get("avgday_f");
        String avgday_t = params.get("avgday_t");
        if (StringUtils.isNotBlank(avgday_f) || StringUtils.isNotBlank(avgday_t)) {
            ranges.add(new Range("avgDayAge", avgday_f, avgday_t));
        }
        return ranges;
    }

    @Override
    protected List<Sort> buildSort(Map<String, String> params) {
        List<Sort> sorts = Lists.newArrayList();
        // 排序, 目前是: 建群日期_日龄_存栏
        String sort = params.get("sort");
        if (StringUtils.isNotBlank(sort)) {
            List<String> parts = Splitters.UNDERSCORE.splitToList(sort);
            if (parts.size() < 3) {
                return sorts;
            }
            String open = Iterables.getFirst(parts, "0");
            String avgDayAge = Iterables.get(parts, 1, "0");
            String quantity = Iterables.get(parts, 2, "0");
            // 新增sort
            sort(sorts, open, "openAt");
            sort(sorts, avgDayAge, "avgDayAge");
            sort(sorts, quantity, "quantity");
        }
        return sorts;
    }
    private void sort(List<Sort> sorts, String part, String field) {
        Sort sort = null;
        switch (Integer.parseInt(part)) {
            case 1:
                sorts.add(new Sort(field, "asc"));
                break;
            case 2:
                sorts.add(new Sort(field, "desc"));
                break;
            default:
                break;
        }
    }

    @Override
    protected List<Highlight> buildHighlight(Map<String, String> params) {
        String highlight = params.get("highlight");
        if (org.springframework.util.StringUtils.hasText(highlight)) {
            List<String> fields = Splitters.UNDERSCORE.splitToList(highlight);
            List<Highlight> highlights = Lists.newArrayListWithCapacity(fields.size());
            for (String field : fields) {
                highlights.add(new Highlight(field));
            }
            return highlights;
        }
        return null;
    }

    @Override
    protected List<Aggs> buildAggs(Map<String, String> params) {
        String aggs = params.get("aggs");
        if(StringUtils.isNotBlank(aggs)){
            List<String> aggSpecifiers = Splitter.on('$').omitEmptyStrings().trimResults().splitToList(aggs);
            List<Aggs> result = Lists.newArrayListWithCapacity(aggSpecifiers.size());
            for (String aggSpecifier : aggSpecifiers) {
                List<String> parts = Splitters.COLON.splitToList(aggSpecifier);
                Aggs agg = new Aggs(parts.get(0), parts.get(1),Integer.parseInt(parts.get(2)));
                result.add(agg);
            }
            return result;
        }
        return null;
    }
}
