package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum SowEvent {

    SOW_ENTRY(1, "母猪进厂事件信息"),
    MATING(2, "母猪配种事件"),
    PREG_CHECK(3, "妊娠检查"),
    FARROWING(4, "分娩"),
    WEAN(5, "断奶事件"),
    FOSTERS(6, "拼窝事件");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private SowEvent(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static SowEvent from(Integer key){
        for(SowEvent sowEvent : SowEvent.values()){
            if(Objects.equals(sowEvent.getKey(), key)){
                return sowEvent;
            }
        }
        return null;
    }

}
