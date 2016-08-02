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

    CHG_LOCATION(1, "转舍" ,"转舍事件"),
    CHG_FARM(2, "转场", "转场事件"),
    CONDITION(3, "体况", "体况事件"),
    DISEASE(4, "疾病" , "疾病事件"),
    VACCINATION(5, "防疫", "免疫事件"),
    REMOVAL(6, "离场", "离场事件"),
    ENTRY(7, "进场","进场事件"),       //进场事件

    SEMEN(8, "采精","公猪采精事件信息"),     //只有公猪有此事件

    MATING(9, "配种", "母猪配种事件"),
    TO_PREG(10, "转入妊娠舍" ,"转入妊娠舍"),
    PREG_CHECK(11, "妊娠检查", "妊娠检查"),
    TO_MATING(12, "转入配种舍", "转入配种舍"),
    ABORTION(13, "流产", "流产事件信息"),
    TO_FARROWING(14, "去分娩", "去分娩信息"),
    FARROWING(15, "分娩" , "分娩"),
    WEAN(16, "断奶", "断奶事件"),
    FOSTERS(17, "拼窝", "拼窝事件"),
    FOSTERS_BY(19, "被拼窝", "被拼窝母猪"),
    PIGLETS_CHG(18, "仔猪变动", "母猪仔猪变动事件信息");


    @Getter
    private Integer key;

    @Getter
    private String name;

    @Getter
    private String desc;

    PigEvent(Integer key, String name, String desc){
        this.key = key;
        this.name = name;
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

    public static PigEvent from(String name){
        for(PigEvent pigEvent : PigEvent.values()){
            if(Objects.equals(pigEvent.name, name)){
                return pigEvent;
            }
        }
        return null;
    }
}
