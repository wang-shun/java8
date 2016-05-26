package io.terminus.doctor.common.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 猪类枚举
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public enum PigType {

    FARROW_PIGLET(1, "产房仔猪", "商品猪"),
    NURSERY_PIGLET(2, "保育猪", "商品猪"),
    FATTEN_PIG(3, "育肥猪", "商品猪"),
    RESERVE_SOW(4, "后备母猪", "种母猪"),
    MATE_SOW(5, "配种母猪", "种母猪"),
    PREG_SOW(6, "妊娠母猪", "种母猪"),
    DELIVER_SOW(7, "分娩母猪", "种母猪"),
    RESERVE_BOAR(8, "后备公猪", "种公猪"),
    BOAR(9, "种公猪", "种公猪");

    @Getter
    private final int value;
    @Getter
    private final String desc;
    @Getter
    private final String type;

    PigType(int value, String desc, String type) {
        this.value = value;
        this.desc = desc;
        this.type = type;
    }

    public static PigType from(int number) {
        for (PigType type : PigType.values()) {
            if (Objects.equal(type.value, number)) {
                return type;
            }
        }
        return null;
    }
}
