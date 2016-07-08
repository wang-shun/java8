package io.terminus.doctor.event.search.query;

import io.terminus.search.api.query.Criterias;
import io.terminus.search.api.query.CriteriasBuilder;
import io.terminus.search.api.query.Range;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;
import lombok.Data;

/**
 * Desc: ES查询条件继承类
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/22
 */
@Data
public class PigCriterias extends Criterias {
    private static final long serialVersionUID = -5195951116006766247L;

    // 前缀搜索字段
    private Prefix prefix;

    // 部分匹配字段(类似like)
    private WildCard wildCard;

    public PigCriterias(CriteriasBuilder cb) {
        super(cb);
        if(this.getRanges() != null && !this.getRanges().isEmpty()) {
            Range t2 = this.getRanges().get(this.getRanges().size() - 1);
            t2.setLast(true);
        } else if(this.getTerms() != null && !this.getTerms().isEmpty()) {
            Terms t1 = this.getTerms().get(this.getTerms().size() - 1);
            t1.setLast(true);
        } else if(this.getTerm() != null && !this.getTerm().isEmpty()) {
            Term t = this.getTerm().get(this.getTerm().size() - 1);
            t.setLast(true);
        }
    }
}
