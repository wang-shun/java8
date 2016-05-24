package io.terminus.doctor.event.search.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: 猪群查询信息
 *      @see IndexedGroup
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchedGroup implements Serializable {
    private static final long serialVersionUID = 2679721994015824531L;
    /**
     * id
     */
    private Long id;

}
