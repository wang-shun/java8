package io.terminus.doctor.event.constants;

import lombok.Getter;

/**
 * Desc: 枚举对应数据id
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */

public enum DoctorBasicEnums {

    DEAD(110L, "死亡"),
    ELIMINATE(111L, "淘汰"),
    SALE(119L, "销售");

    @Getter
    private final long id;
    @Getter
    private final String name;

    DoctorBasicEnums(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
