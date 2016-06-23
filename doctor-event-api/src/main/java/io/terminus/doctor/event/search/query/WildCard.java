package io.terminus.doctor.event.search.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: ES 通配符搜索
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WildCard implements Serializable {
    private static final long serialVersionUID = 8507348410024711887L;

    private String field;
    private String value;
}
