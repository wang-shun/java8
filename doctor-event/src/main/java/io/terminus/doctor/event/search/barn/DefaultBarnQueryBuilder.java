package io.terminus.doctor.event.search.barn;

import io.terminus.search.api.query.Aggs;
import io.terminus.search.api.query.Highlight;
import io.terminus.search.api.query.Keyword;
import io.terminus.search.api.query.Range;
import io.terminus.search.api.query.Sort;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;

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
        return null;
    }

    @Override
    protected List<Term> buildTerm(Map<String, String> params) {
        return null;
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
    protected List<Sort> buildSort(Map<String, String> params) {
        return null;
    }

    @Override
    protected List<Highlight> buildHighlight(Map<String, String> params) {
        return null;
    }

    @Override
    protected List<Aggs> buildAggs(Map<String, String> params) {
        return null;
    }
}
