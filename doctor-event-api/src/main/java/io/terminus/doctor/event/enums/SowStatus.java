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
    NotSurePregnancy(5,"妊娠检查无法确定"),
    KongHuai(6,"空怀"),
    Abortion(7,"流产"),
    Farrow(8,"等待分娩"),
    FEED(9, "哺乳状态"),
    Wean(10,"断奶"),
    FanQing(11,"返情");

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
