package io.terminus.doctor.basic.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 用户搜索类型
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */

public enum SearchType {

    /**
     * 母猪
     */
    SOW(1, "母猪"),

    /**
     * 公猪
     */
    BOAR(2, "公猪"),

    /**
     * 猪群
     */
    GROUP(3, "猪群"),

    /**
     * 猪舍
     */
    BARN(4, "猪舍");

    @Getter
    private int value;

    @Getter
    private String describe;

    SearchType(int value, String describe) {
        this.value = value;
        this.describe = describe;
    }

    public static SearchType from(int value) {
        for (SearchType type : SearchType.values()) {
            if (Objects.equal(type.value, value)) {
                return type;
            }
        }
        return null;
    }
}
