package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by xjn on 17/4/22.
 */
public enum InType {
    PIGLET(1, "仔猪转入"),
    SEED(2, "种猪转商品猪"),
    GROUP(3, "群间转移"),
    BUY(4, "购买");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    InType(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static InType from(Integer value){
        for (InType type : InType.values()){
            if(Objects.equals(value, type.getValue())){
                return type;
            }
        }
        return null;
    }

    public static InType from(String desc){
        for (InType type : InType.values()){
            if(Objects.equals(desc, type.desc)){
                return type;
            }
        }
        return null;
    }
}
