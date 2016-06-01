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
    KongHuai(5,"空怀"),
    Abortion(6,"流产"),
    Farrow(7,"等待分娩"),
    FEED(8, "哺乳状态"),
    Wean(9,"断奶"),
    FanQing(10,"返情");

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
