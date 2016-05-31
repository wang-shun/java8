package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 猪来源信息
 */
public enum PigSource {

    LOCAL(1,"bc","本场"),
    OUTER(2,"wg","外购");

    @Getter
    private Integer key;

    @Getter
    private String inputCode;

    @Getter
    private String desc;

    PigSource(Integer key, String inputCode, String desc){
        this.key = key;
        this.inputCode = inputCode;
        this.desc = desc;
    }

    public static PigSource from(Integer key){
        for(PigSource pigSource : PigSource.values()){
            if(Objects.equals(pigSource.getKey(), key)){
                return pigSource;
            }
        }
        return null;
    }
}
