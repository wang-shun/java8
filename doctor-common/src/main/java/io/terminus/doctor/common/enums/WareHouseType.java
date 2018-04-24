package io.terminus.doctor.common.enums;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
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

    //所有类型
    public static final List<Integer> ALL_TYPES = Lists.newArrayList(
            FEED.getKey(),
            MATERIAL.getKey(),
            VACCINATION.getKey(),
            MEDICINE.getKey(),
            CONSUME.getKey()
    );

    //所有类型名称
    public static final List<String> ALL_TYPES_DESC = Lists.newArrayList(
            FEED.getDesc(),
            MATERIAL.getDesc(),
            VACCINATION.getDesc(),
            MEDICINE.getDesc(),
            CONSUME.getDesc()
    );

    public static WareHouseType from(Integer key){
        for(WareHouseType wareHouseType : WareHouseType.values()){
            if(Objects.equals(wareHouseType.getKey(), key)){
                return wareHouseType;
            }
        }
        return null;
    }

    public static WareHouseType from(String desc){
        for(WareHouseType wareHouseType : WareHouseType.values()){
            if(Objects.equals(wareHouseType.getDesc(), desc)){
                return wareHouseType;
            }
        }
        return null;
    }
}
