package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by xiao on 16/8/17.
 */
public enum KongHuaiStarus {
    YING(2, "妊娠检查阴性"),
    LIUCHAN(3, "流产"),
    FANQING(4, "返情");

    @Getter
    private Integer key;

    @Getter
    private String name;

    KongHuaiStarus(Integer key, String name) {
        this.key = key;
        this.name = name;
    }
    public static KongHuaiStarus from(Integer key){
        for(KongHuaiStarus kongHuaiPregCheckResult : KongHuaiStarus.values()){
            if(Objects.equals(kongHuaiPregCheckResult.getKey(), key)){
                return kongHuaiPregCheckResult;
            }
        }
        return null;
    }

}
