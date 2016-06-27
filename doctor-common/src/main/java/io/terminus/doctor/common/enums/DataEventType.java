package io.terminus.doctor.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-24
 * Email:yaoqj@terminus.io
 * Descirbe: 事件信息的枚举方式
 */
public enum DataEventType {

    PigEventCreate(1, "猪创建事件信息"),
    VaccinationMedicalConsume(2, "免疫药品领用事件"),
    MaterialInfoCreateEvent(3, "物料信息创建事件信息"),
    GroupEventCreate(4, "猪群创建事件信息"),
    BarnUpdate(5, "猪舍变动信息"),
    BasicUpdate(6, "基础数据变动信息");

    @Getter
    private int key;

    @Getter
    private String desc;

    DataEventType(int key, String desc){
        this.key = key;
        this.desc = desc;
    }

    public static DataEventType from(Integer key){
        for(DataEventType dataEventType : DataEventType.values()){
            if(Objects.equals(dataEventType.getKey(), key)){
                return dataEventType;
            }
        }
        return null;
    }
}
