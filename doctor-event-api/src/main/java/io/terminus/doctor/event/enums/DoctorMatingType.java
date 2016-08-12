package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

public enum DoctorMatingType {

    HP(1, "后备到配种"),
    LPC(2, "流产到配种(妊娠检查)"),
    LPL(3, "流产到配种(流产事件)"),
    DP(4, "断奶到配种"),
    YP(5, "阴性到配种"),
    FP(6, "返情到配种");

    @Getter
    private Integer key;

    @Getter
    private String desc;

    DoctorMatingType(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public static DoctorMatingType from(Integer key) {
        for (DoctorMatingType matingType : DoctorMatingType.values()) {
            if (Objects.equals(key, matingType.getKey())) {
                return matingType;
            }
        }
        return null;
    }

    public static DoctorMatingType from(String desc) {
        for (DoctorMatingType matingType : DoctorMatingType.values()) {
            if (Objects.equals(desc, matingType.desc)) {
                return matingType;
            }
        }
        return null;
    }
}
