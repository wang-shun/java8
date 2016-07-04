package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-04
 * Email:yaoqj@terminus.io
 * Descirbe: 猪群性别
 */
public enum PigSex {

    MIX(1, "混合"),
    SOW(2, "母猪"),
    BOAR(3, "公猪");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private PigSex(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static PigSex from(Integer key){
        for(PigSex pigSex : PigSex.values()){
            if(Objects.equals(pigSex.getKey(), key)){
                return pigSex;
            }
        }
        return null;
    }
}
