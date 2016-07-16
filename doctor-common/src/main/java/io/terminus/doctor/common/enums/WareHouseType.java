package io.terminus.doctor.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 统计对应的仓库物料类型
 */
public enum WareHouseType {

    FEED(1, "饲料"),
    MATERIAL(2, "原料"),
    VACCINATION(3, "疫苗"),
    MEDICINE(4, "药品"),
    CONSUME(5,"消耗品");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    WareHouseType(Integer key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static WareHouseType from(Integer key){
        for(WareHouseType wareHouseType : WareHouseType.values()){
            if(Objects.equals(wareHouseType.getKey(), key)){
                return wareHouseType;
            }
        }
        return null;
    }
}
