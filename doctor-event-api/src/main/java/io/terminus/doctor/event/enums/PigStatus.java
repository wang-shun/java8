package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum PigStatus {

    // 公猪状态信息
    BOAR_ENTRY(11,"公猪已进场", "公猪已进场"),
    BOAR_LEAVE(12,"公猪已离场", "公猪已离场"),

    // 母猪状态
    Entry (1,"进场","进场"),
    Removal(2,"已离场","已离场"),
    Mate(3,"已配种","已配种"),
    Pregnancy(4,"阳性","妊娠检查阳性"),
    KongHuai(5,"空怀","空怀"),
    Farrow(7,"待分娩","待分娩"),
    FEED(8,"哺乳","哺乳"),
    Wean(9,"断奶","断奶");

    @Getter
    private Integer key;

    @Getter
    private String name;

    @Getter
    private String desc;

    PigStatus(Integer key, String name, String desc){
        this.key = key;
        this.name = name;
        this.desc = desc;
    }

    public static PigStatus from(Integer key){
        for(PigStatus pigStatus : PigStatus.values()){
            if(Objects.equals(pigStatus.getKey(), key)){
                return pigStatus;
            }
        }
        return null;
    }

    public static PigStatus from(String desc){
        for(PigStatus pigStatus : PigStatus.values()){
            if(Objects.equals(pigStatus.desc, desc)){
                return pigStatus;
            }
        }
        return null;
    }
}
