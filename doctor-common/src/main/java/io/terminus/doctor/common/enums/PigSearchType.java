package io.terminus.doctor.common.enums;

import lombok.Getter;

/**
 * Desc: 猪舍列表查询详情的类别区分
 * @see PigType
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/30
 */
public enum PigSearchType {

    BOAR(1, "公猪"),
    SOW(2, "母猪"),
    GROUP(3, "猪群");

    @Getter
    private final int value;
    @Getter
    private final String desc;

    PigSearchType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
