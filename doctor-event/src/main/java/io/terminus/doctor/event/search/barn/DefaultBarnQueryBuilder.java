package io.terminus.doctor.event.search.barn;

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
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public class DefaultBarnQueryBuilder extends BaseBarnQueryBuilder {
    @Override
    protected Keyword buildKeyword(Map<String, String> params) {
        // 处理关键词
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            return new Keyword(ImmutableList.of("name"), q);
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
        // 4. 状态
        term(termList, params, "status");
        term(termList, params, "canOpenGroup");
        // 5. 工作人员
        term(termList, params, "staffId");

        return termList;
    }

    private void term(List<Term> termList, Map<String, String> params, String key) {
        String value = params.get(key);
        if (StringUtils.isNotBlank(value)) {
            termList.add(new Term(key, value));
        }
    }

    @Override
    protected List<Sort> buildSort(Map<String, String> params) {
        List<Sort> sorts = Lists.newArrayList();
        // 排序: 存栏_创建日期_容量
        String sort = params.get("sort");
        if (StringUtils.isNotBlank(sort)) {
            List<String> parts = Splitters.UNDERSCORE.splitToList(sort);
            if (parts.size() < 3) {
                return sorts;
            }
            String storage = Iterables.getFirst(parts, "0");
            String createdAt = Iterables.get(parts, 1, "0");
            String capacity = Iterables.get(parts, 2, "0");
            // 新增sort
            sort(sorts, storage, "storage");
            sort(sorts, createdAt, "createdAt");
            sort(sorts, capacity, "capacity");
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
    protected List<Terms> buildTerms(Map<String, String> params) {
        return null;
    }

    @Override
    protected List<Range> buildRanges(Map<String, String> params) {
        return null;
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
        if (StringUtils.isNotBlank(aggs)) {
            List<String> aggSpecifiers = Splitter.on('$').omitEmptyStrings().trimResults().splitToList(aggs);
            List<Aggs> result = Lists.newArrayListWithCapacity(aggSpecifiers.size());
            for (String aggSpecifier : aggSpecifiers) {
                List<String> parts = Splitters.COLON.splitToList(aggSpecifier);
                Aggs agg = new Aggs(parts.get(0), parts.get(1), Integer.parseInt(parts.get(2)));
                result.add(agg);
            }
            return result;
        }
        return null;
    }
}
