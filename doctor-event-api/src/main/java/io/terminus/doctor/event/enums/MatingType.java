package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪配种类型
 */
public enum MatingType {

    NATURAL(1, "自然交配"),
    MANUAL(2, "人工交配");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    MatingType(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static MatingType from(Integer key){
        for (MatingType matingType : MatingType.values()){
            if(Objects.equals(key, matingType.getKey())){
                return matingType;
            }
        }
        return null;
    }

    public static MatingType from(String desc){
        for (MatingType matingType : MatingType.values()){
            if(Objects.equals(desc, matingType.desc)){
                return matingType;
            }
        }
        return null;
    }
}
