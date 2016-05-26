package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum PregCheckResult {
    YANG(1, "rsjcyx", "妊娠检查阳性"),
    LIUCHAN(2, "lc", "流产"),
    FANQING(3, "fq", "返情");

    @Getter
    private Integer key;

    @Getter
    private String inputCode; //输入码

    @Getter
    private String desc;

    private PregCheckResult(Integer key, String inputCode, String desc){
        this.key = key;
        this.inputCode = inputCode;
        this.desc = desc;
    }

    public static PregCheckResult from(Integer key){
        for(PregCheckResult pregCheckResult : PregCheckResult.values()){
            if(Objects.equals(pregCheckResult.getKey(), key)){
                return pregCheckResult;
            }
        }
        return null;
    }

}
