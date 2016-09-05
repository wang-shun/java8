package io.terminus.doctor.common.enums;

import lombok.Getter;

/**
 * Created by xiao on 16/9/5.
 */
public enum  SearchType {

        BOAR(1, "公猪"),
        SOW(2, "母猪"),
        GROUP(3, "猪群"),
    MATERIAL(4, "母群共存");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        SearchType(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
}
