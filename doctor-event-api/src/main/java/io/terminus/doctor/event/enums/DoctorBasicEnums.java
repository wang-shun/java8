package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 枚举对应数据id
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */

public enum DoctorBasicEnums {

    SALE(109L, "销售"),
    DEAD(110L, "死亡"),
    ELIMINATE(111L, "淘汰"),
    LOST(112L, "失踪"),
    OTHER(113L, "其他"),
    KILL(114L, "自宰");

    @Getter
    private final long id;
    @Getter
    private final String name;

    DoctorBasicEnums(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static DoctorBasicEnums from(String name) {
        for (DoctorBasicEnums type : DoctorBasicEnums.values()) {
            if (Objects.equal(type.name, name)) {
                return type;
            }
        }
        return null;
    }
}
