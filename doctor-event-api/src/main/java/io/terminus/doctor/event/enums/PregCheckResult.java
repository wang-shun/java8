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
    UNSURE(0, "rsjcbqd","妊娠检查不确定"),
    YANG(1, "rsjcyx", "妊娠检查阳性"),
    YING(2, "rsjcyx", "妊娠检查阴性"),
    LIUCHAN(3, "lc", "流产"),
    FANQING(4, "fq", "返情");

    @Getter
    private Integer key;

    @Getter
    private String inputCode; //输入码

    @Getter
    private String desc;

    PregCheckResult(Integer key, String inputCode, String desc){
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

    public static PregCheckResult from(String desc){
        for(PregCheckResult pregCheckResult : PregCheckResult.values()){
            if(Objects.equals(pregCheckResult.desc, desc)){
                return pregCheckResult;
            }
        }
        return null;
    }
}
