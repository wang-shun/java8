package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe: 公猪状态信息
 */
public enum BoarStatus {

    ENTRY(1, "已进场"),
    LEAVE(2, "已离场");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private BoarStatus(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static BoarStatus from(Integer key){
        for(BoarStatus boarStatus : BoarStatus.values()){
            if(Objects.equals(boarStatus.getKey(), key)){
                return boarStatus;
            }
        }
        return null;
    }
}
