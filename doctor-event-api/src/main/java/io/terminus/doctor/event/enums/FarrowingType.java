package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 分娩类型
 */
public enum FarrowingType {

    USUAL(1, "pt","普通"),
    EARLY(2,"zc", "早产"),
    HELP(3, "zc","助产");

    @Getter
    private Integer key;

    @Getter
    private String inputCode;

    @Getter
    private String desc;

    FarrowingType(Integer key, String inputCode, String desc){
        this.key = key;
        this.inputCode = inputCode;
        this.desc = desc;
    }

    public static FarrowingType from(Integer key){
        for(FarrowingType farrowingType : FarrowingType.values()){
            if(Objects.equals(key, farrowingType.getKey())){
                return farrowingType;
            }
        }
        return null;
    }

    public static FarrowingType from(String desc){
        for(FarrowingType farrowingType : FarrowingType.values()){
            if(Objects.equals(desc, farrowingType.desc)){
                return farrowingType;
            }
        }
        return null;
    }
}
