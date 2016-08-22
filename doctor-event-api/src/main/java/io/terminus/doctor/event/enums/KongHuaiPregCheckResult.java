package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by xiao on 16/8/17.
 */
public enum KongHuaiPregCheckResult {
    YING(51, "妊娠检查阴性"),
    LIUCHAN(52, "流产"),
    FANQING(53, "返情");

    @Getter
    private Integer key;

    @Getter
    private String name;

    KongHuaiPregCheckResult(Integer key, String name) {
        this.key = key;
        this.name = name;
    }
    public static KongHuaiPregCheckResult from(Integer key){
        for(KongHuaiPregCheckResult kongHuaiPregCheckResult : KongHuaiPregCheckResult.values()){
            if(Objects.equals(kongHuaiPregCheckResult.getKey(), key)){
                return kongHuaiPregCheckResult;
            }
        }
        return null;
    }

}
