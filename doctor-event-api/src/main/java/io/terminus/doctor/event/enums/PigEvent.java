package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum PigEvent {

    CHG_LOCATION(1, "转舍事件"),
    CHG_FARM(2,"转场事件"),
    CONDITION(3, "体况事件"),
    DISEASE(4, " 疾病事件"),
    VACCINATION(5, "免疫事件"),
    REMOVAL(6, "离场事件"),
    ENTRY(7, "进厂事件"),

    SEMEN(8, "公猪采精事件信息"),

    MATING(9, "母猪配种事件"),
    PREG_CHECK(10, "妊娠检查"),
    FARROWING(11, "分娩"),
    WEAN(12, "断奶事件"),
    FOSTERS(13, "拼窝事件");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private PigEvent(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static PigEvent from(Integer key){
        for(PigEvent pigEvent : PigEvent.values()){
            if(Objects.equals(pigEvent.getKey(), key)){
                return pigEvent;
            }
        }
        return null;
    }
}
