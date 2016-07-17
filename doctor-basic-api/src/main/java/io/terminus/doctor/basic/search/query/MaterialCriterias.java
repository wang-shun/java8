package io.terminus.doctor.basic.search.query;

import io.terminus.search.api.query.Criterias;
import io.terminus.search.api.query.CriteriasBuilder;
import io.terminus.search.api.query.Range;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 物料自定义搜索
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/2
 */
@Data
public class MaterialCriterias extends Criterias implements Serializable {

    private static final long serialVersionUID = -8161707677341407065L;

    // 不包含的terms
    private MustNotTerms mustNotTerms;


    public MaterialCriterias(CriteriasBuilder cb) {
        super(cb);
        if(this.getRanges() != null && !this.getRanges().isEmpty()) {
            Range range = this.getRanges().get(this.getRanges().size() - 1);
            range.setLast(true);
        } else if(this.getTerms() != null && !this.getTerms().isEmpty()) {
            Terms terms = this.getTerms().get(this.getTerms().size() - 1);
            terms.setLast(true);
        } else if(this.getTerms() != null && !this.getTerms().isEmpty()) {
            Term term = this.getTerm().get(this.getTerm().size() - 1);
            term.setLast(true);
        }

        if (this.mustNotTerms != null) {
            List<Terms> notTerms = this.mustNotTerms.getTerms();
            if (notTerms != null && !notTerms.isEmpty()) {
                Terms terms = notTerms.get(notTerms.size() - 1);
                terms.setLast(true);
            }
            List<Term> notTerm = this.mustNotTerms.getTerm();
            if (notTerm != null && !notTerm.isEmpty()) {
                Term term = notTerm.get(notTerm.size() - 1);
                term.setLast(true);
            }
        }
    }
}
