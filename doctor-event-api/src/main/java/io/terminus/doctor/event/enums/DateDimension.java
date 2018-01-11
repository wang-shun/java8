package io.terminus.doctor.event.enums;


import lombok.Getter;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 */
public enum DateDimension {
    DAY(1, "日"),
    WEEK(2, "周"),
    MONTH(3, "月"),
    QUARTER(4, "季"),
    YEAR(5, "年");

    @Getter
    private Integer value;
    @Getter
    private String name;

    DateDimension(Integer value, String name) {
        this.value = value;
        this.name = name;
    }
}
