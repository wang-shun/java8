package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Desc: 免疫日期类型枚举
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/13
 */
public enum VaccinationDateType {

    // 常规
    FIXED_DAY_AGE(1, "固定日龄"),
    FIXED_DATE(2, "固定日期"),
    FIXED_WEIGHT(3, "固定体重"),

    // 猪常规
    CHANGE_LOC(4, "转舍"),
    CHANGE_GROUP(5, "转群"),

    // 母猪
    PREG_CHECK(6, "妊娠检查"),
    BREEDING(7, "配种"),
    DELIVER(8, "分娩"),
    WEAN(9, "断奶"),

    //(jsj后面加的)
    BACK_TO_LOVE(10,"返情"),
    MISCARRY(11,"流产"),
    FEMININE(12,"阴性"),
    ENTER(13,"进场");

    @Getter
    private Integer value;

    @Getter
    private String desc;

    VaccinationDateType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static VaccinationDateType from(Integer value){
        for(VaccinationDateType vaccinationDateType : VaccinationDateType.values()){
            if(Objects.equals(value, vaccinationDateType.getValue())){
                return vaccinationDateType;
            }
        }
        return null;
    }
}
