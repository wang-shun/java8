package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 是否
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */

public enum IsOrNot {
    NO(0, "否"),
    YES(1, "是");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    IsOrNot(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static IsOrNot from(String desc) {
        for (IsOrNot is : IsOrNot.values()) {
            if (Objects.equal(is.desc, desc)) {
                return is;
            }
        }
        return null;
    }
}
