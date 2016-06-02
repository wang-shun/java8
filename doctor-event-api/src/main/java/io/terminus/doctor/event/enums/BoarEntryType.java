package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum BoarEntryType {

    HGZ(1, "hgz", "活公猪"),
    LDJY(2,"ldjy","冷冻精液"),
    XXJY(3,"xxjy", "新鲜精液");

    @Getter
    private Integer key;

    @Getter
    private String code;

    @Getter
    private String desc;

    private BoarEntryType(Integer key, String code, String desc){
        this.key = key;
        this.code = code;
        this.desc = desc;
    }

    public static BoarEntryType from(Integer key){
        for(BoarEntryType boarEntryType : BoarEntryType.values()){
            if(Objects.equals(boarEntryType.getKey(), key)){
                return boarEntryType;
            }
        }
        return null;
    }

}
