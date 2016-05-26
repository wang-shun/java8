package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum BoarEvent {

    SEMEN(1, "公猪采精事件信息"),
    CONDITION(2, "公猪体况事件");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private BoarEvent(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static BoarEvent from(Integer key){
        for(BoarEvent boarEvent : BoarEvent.values()){
            if(Objects.equals(boarEvent.getKey(), key)){
                return boarEvent;
            }
        }
        return null;
    }
}
