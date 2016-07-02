package io.terminus.doctor.warehouse.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum IsOrNot {

    YES(1, "true"),
    NO(0, "false");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private IsOrNot(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static IsOrNot from(Integer key){
        for(IsOrNot isOrNot: IsOrNot.values()){
            if(Objects.equals(isOrNot.getKey(), key)){
                return isOrNot;
            }
        }
        return null;
    }
}
