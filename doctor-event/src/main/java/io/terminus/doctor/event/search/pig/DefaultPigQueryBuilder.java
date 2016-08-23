package io.terminus.doctor.event.search.pig;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.event.search.query.Prefix;
import io.terminus.doctor.event.search.query.WildCard;
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
 * Desc: 默认猪(索引对象)查询条件构建
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
public class DefaultPigQueryBuilder extends BasePigQueryBuilder {

    @Override
    protected Keyword buildKeyword(Map<String, String> params) {
        // 搜索关键词 处理
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            // return new Keyword(ImmutableList.of("pigCode"), q);
            return new Keyword(ImmutableList.of("pigCodeSearch"), q);
        }
        return null;
    }

    @Override
    protected Prefix buildPrefix(Map<String, String> params) {
        // 猪号使用搜索前缀查询搜索前缀
        /* String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            //  return new Prefix("pigCode", q);
            return new Prefix("pigCodeSearch", q);
        }*/
        return null;
    }

    @Override
    protected WildCard buildWildCard(Map<String, String> params) {
        // 猪号使用搜索通配符查询搜索前缀
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            // return new WildCard("pigCode", "*" + q + "*");
            return new WildCard("pigCodeSearch", "*" + q + "*");
        }
        return null;
    }

    @Override
    protected List<Term> buildTerm(Map<String, String> params) {
        List<Term> termList = Lists.newArrayList();
        // 公司id
        term(termList, params, "orgId", "orgId");
        // 猪场id
        term(termList, params, "farmId", "farmId");
        // 猪舍
        term(termList, params, "barnId", "currentBarnId");
        // 猪类型pigType
        term(termList, params, "pigType", "pigType");
        // 品种
        term(termList, params, "breedId", "breedId");
        // 品系
        term(termList, params, "geneticId","geneticId");
        // 状态
        term(termList, params, "status", "status");
        //是否离场
        term(termList, params, "isRemoval", "isRemoval");
        return termList;
    }
    private void term(List<Term> termList, Map<String, String> params, String key, String field) {
        String value = params.get(key);
        if (StringUtils.isNotBlank(value)) {
            termList.add(new Term(field, value));
        }
    }

    @Override
    protected List<Terms> buildTerms(Map<String, String> params) {
        List<Terms> termsList = Lists.newArrayList();
        // 1. 多状态
        String statuses = params.get("statuses");
        if (StringUtils.isNotBlank(statuses)) {
            List<String> statusList = Splitters.UNDERSCORE.splitToList(statuses);
            termsList.add(new Terms("status", statusList));
        }

        // 2. 当前用户所拥有权限的猪舍
        String barnIds = params.get("barnIds");
        if (StringUtils.isNotBlank(barnIds)){
            List<String> barnIdList = Splitters.COMMA.splitToList(barnIds);
            termsList.add(new Terms("currentBarnId", barnIdList));
        }
        return termsList;
    }

    @Override
    protected List<Range> buildRanges(Map<String, String> params) {
        // 暂无范围
        return null;
    }

    @Override
    protected List<Sort> buildSort(Map<String, String> params) {
        // 默认按updatedAt进行降序排序
        List<Sort> sorts = Lists.newArrayList();
        sort(sorts, "2", "updatedAt");

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
