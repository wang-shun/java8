package io.terminus.doctor.event.enums;


import lombok.Getter;

import java.util.Objects;

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

    public static DateDimension from(Integer value){
        for(DateDimension dateDimension : DateDimension.values()){
            if(Objects.equals(dateDimension.getValue(), value)){
                return dateDimension;
            }
        }
        return null;
    }

    public static DateDimension from(String name){
        for(DateDimension dateDimension : DateDimension.values()){
            if(Objects.equals(dateDimension.getName(), name)){
                return dateDimension;
            }
        }
        return null;
    }
}
