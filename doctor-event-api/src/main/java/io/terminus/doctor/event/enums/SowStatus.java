package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum SowStatus {

    Entry (1,"待配种"),
    Removal(2,"已离场"),
    Mate(3,"已配种"),
    Pregnancy(4,"妊娠检查阳性"),
    Hy(5,"怀孕"),
    NoPregnancy(6,"妊娠检查阴性"),
    NotSurePregnancy(7,"妊娠检查无法确定"),
    Farrow(8,"哺乳"),
    Wean(9,"断奶"),
    KongHuai(10,"空怀"),
    FanQing(11,"返情"),
    Abortion(12,"流产");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    private SowStatus(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static SowStatus from(Integer key){
        for(SowStatus sowStatus : SowStatus.values()){
            if(Objects.equals(sowStatus.getKey(), key)){
                return sowStatus;
            }
        }
        return null;
    }
}
