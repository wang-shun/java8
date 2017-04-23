package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by xjn on 17/4/22.
 */
public enum VaccinResult {
    POSITIVE(1, "阳性"),
    NEGATIVE(-1, "阴性");

    @Getter
    private final int value;
    @Getter
    private final String desc;

    VaccinResult(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static VaccinResult from(String desc) {
        for (VaccinResult result : VaccinResult.values()) {
            if (Objects.equals(result.desc, desc)) {
                return result;
            }
        }
        return null;
    }

    public static VaccinResult from(int value) {
        for (VaccinResult result : VaccinResult.values()) {
            if (Objects.equals(result.value, value)) {
                return result;
            }
        }
        return null;
    }
}
