package io.terminus.doctor.event.search.barn;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪舍ElasticSearch索引
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
public class IndexedBarn implements Serializable {
    private static final long serialVersionUID = 7546040680187245833L;

    private Long id;
}
