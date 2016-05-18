package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public enum VaccinationResult {
    YING(1, "yx", "阴性"),
    YANG(2, "yx", "阳性");

    @Getter
    private Integer key;

    @Getter
    private String inputCode;

    @Getter
    private String desc;

    private VaccinationResult(Integer key, String inputCode, String desc){
        this.desc = desc;
        this.inputCode = inputCode;
        this.desc = desc;
    }

    public static VaccinationResult from(Integer key){
        for(VaccinationResult vaccinationResult : VaccinationResult.values()){
            if(Objects.equals(key, vaccinationResult.getKey())){
                return vaccinationResult;
            }
        }
        return null;
    }
}
