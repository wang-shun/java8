package io.terminus.doctor.event.dto;

import io.terminus.common.model.Paging;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪群或猪分页结果(增加数量显示)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/15
 */

public class GroupPigPaging<T> extends Paging<T> implements Serializable {
    private static final long serialVersionUID = -3933777181108443820L;

    public GroupPigPaging() {}

    public GroupPigPaging(Long total, List<T> data) {
        super(total, data);
    }

    public GroupPigPaging(Paging<T> paging, Long count, Long sowCount) {
        super(paging.getTotal(), paging.getData());
        this.count = count;
        this.sowCount = sowCount;
    }

    //猪群数量
    private Long count;

    //母猪数量
    private Long sowCount;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getSowCount() {
        return sowCount;
    }

    public void setSowCount(Long sowCount) {
        this.sowCount = sowCount;
    }
}
