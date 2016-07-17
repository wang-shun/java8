package io.terminus.doctor.basic.search.material;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.search.query.MyKeyWord;
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
 * Desc: 物料查询builder
 * Mail: chk@terminus.io
 * author: IceMimosa
 * Date: 16/6/16
 */
public class DefaultMaterialQueryBuilder extends BaseMaterialQueryBuilder {
    @Override
    protected Keyword buildKeyword(Map<String, String> params) {
        // 处理关键词
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            return new MyKeyWord("materialName", q);
        }
        return null;
    }

    @Override
    protected List<Term> buildTerm(Map<String, String> params) {
        List<Term> termList = Lists.newArrayList();
        // 1. 猪场id TODO: 暂不处理
        // term(termList, params, "farmId");
        // 2. 物料类型
        term(termList, params, "type");

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
            if (parts.size() < 1) {
                return sorts;
            }
            String price = Iterables.getFirst(parts, "0");
            // 新增sort
            sort(sorts, price, "price");
        }
        // 否则默认按 updatedAt 降序
        else {
            sort(sorts, "2", "updatedAt");
        }
        return sorts;
    }

    private void sort(List<Sort> sorts, String part, String field) {
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
        List<Terms> termsList = Lists.newArrayList();
        // 1. 多类型
        String types = params.get("types");
        if (StringUtils.isNotBlank(types)) {
            List<String> typeList = Splitters.UNDERSCORE.splitToList(types);
            termsList.add(new Terms("type", typeList));
        }
        return termsList;
    }

    @Override
    protected List<Terms> buildNotMustTerms(Map<String, String> params) {
        List<Terms> termsList = Lists.newArrayList();
        // 1. 不包含的物料 ids
        String exIds = params.get("exIds");
        if (StringUtils.isNotBlank(exIds)) {
            List<String> exIdsList = Splitters.COMMA.splitToList(exIds);
            termsList.add(new Terms("id", exIdsList));
        }
        return termsList;
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
