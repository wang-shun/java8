package io.terminus.doctor.event.search.query;

import io.terminus.search.api.query.Criterias;
import io.terminus.search.api.query.CriteriasBuilder;
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

    private Prefix prefix;

    private WildCard wildCard;

    public PigCriterias(CriteriasBuilder cb) {
        super(cb);
    }
}
