package io.terminus.doctor.user.interfaces.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PagingDto<T> implements Serializable {
    private static final long serialVersionUID = 8154701850585775441L;

    private Long total;

    private List<T> data;

    public PagingDto() {
    }

    public PagingDto(Long total, List<T> data) {
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Boolean isEmpty() {
        return Objects.equals(0L, total) || data == null || data.isEmpty();
    }

    @SuppressWarnings("all")
    public static <T> PagingDto<T> empty(Class<T> clazz) {
        List<T> emptyList = Collections.emptyList();
        return new PagingDto<T>(0L, emptyList);
    }

    public static <T> PagingDto<T> empty() {
        return new PagingDto<T>(0L, Collections.<T>emptyList());
    }

}
