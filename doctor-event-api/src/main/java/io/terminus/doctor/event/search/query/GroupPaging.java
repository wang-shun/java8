package io.terminus.doctor.event.search.query;

import io.terminus.common.model.Paging;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪群分页结果
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/15
 */

public class GroupPaging<T> extends Paging<T> implements Serializable {
    private static final long serialVersionUID = -3933777181108443820L;

    public GroupPaging() {}

    public GroupPaging(Long total, List<T> data) {
        super(total, data);
    }

    public GroupPaging(Long total, List<T> data, Long count) {
        super(total, data);
        this.count = count;
    }

    //自定义的数量字段
    private Long count;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
