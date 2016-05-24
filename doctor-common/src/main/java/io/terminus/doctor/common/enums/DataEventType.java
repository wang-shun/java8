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

    PigEventCreate(1l, "猪创建事件信息");

    @Getter
    private Long key;

    @Getter
    private String desc;

    private DataEventType(Long key, String desc){
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
