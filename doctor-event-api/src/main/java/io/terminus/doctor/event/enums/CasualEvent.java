package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe: 普通的事件信息
 */
public enum CasualEvent {

    CHG_LOCATION(1, "转场事件"),
    CHG_FARM(2,"转场事件"),
    CONDITION(3, "体况事件"),
    DISEASE(4, " 疾病事件"),
    VACCINATION(5, "免疫事件"),
    REMOVAL(6, "离场事件");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private CasualEvent(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static CasualEvent from(Integer key){
        for(CasualEvent casualEvent : CasualEvent.values()){
            if(Objects.equals(casualEvent.getKey(), key)){
                return casualEvent;
            }
        }
        return null;
    }
}
