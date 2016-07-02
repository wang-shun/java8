package io.terminus.doctor.warehouse.search.query;

import com.google.common.base.Throwables;
import io.terminus.search.api.query.Term;
import io.terminus.search.api.query.Terms;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Desc: 不包含的fields
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class MustNotTerms implements Serializable {
    private static final long serialVersionUID = -3943888130952768965L;

    private List<Term> term;
    private List<Terms> terms;

    public void setTerms(List<Terms> termsList) {
        if(!CollectionUtils.isEmpty(termsList)) {
            this.terms = termsList;
            Iterator var2 = this.terms.iterator();
            while(var2.hasNext()) {
                Terms tms = (Terms)var2.next();
                List values = tms.getValues();
                if(values != null && !values.isEmpty()) {
                    try{
                        Field last = values.get(values.size() - 1).getClass().getDeclaredField("last");
                        last.setAccessible(true);
                        last.setBoolean(values.get(values.size() - 1), true);
                    } catch (Exception e) {
                        log.error("MustNotTerms set Terms last failed, cause by {}", Throwables.getStackTraceAsString(e));
                    }
                }
            }
        }
    }
}
