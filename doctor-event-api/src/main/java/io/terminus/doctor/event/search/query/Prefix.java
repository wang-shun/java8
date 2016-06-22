package io.terminus.doctor.event.search.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: ES前缀搜索
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Prefix implements Serializable {
    private static final long serialVersionUID = -6346534552313797823L;

    private String field;
    private String value;

}
