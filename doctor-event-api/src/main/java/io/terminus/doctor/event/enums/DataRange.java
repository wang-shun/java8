package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-18
 * Email:yaoqj@terminus.io
 * Descirbe: 数据Range 范围信息
 */
public enum DataRange {

    FARM(1,"猪场"),
    ORG(2, "公司"),
    PLAT(3, "平台");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private DataRange(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static DataRange from(Integer key){
        for(DataRange dataRange : DataRange.values()){
            if(Objects.equals(key, dataRange.getKey())){
                return dataRange;
            }
        }
        return null;
    }

}
