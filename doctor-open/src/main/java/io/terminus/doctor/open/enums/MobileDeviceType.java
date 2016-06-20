package io.terminus.doctor.open.enums;

import com.google.common.base.Objects;

/**
 * 陈增辉 on 16/6/20.
 * 移动设备类型枚举
 */
public enum MobileDeviceType {
    ANDROID(1),
    IOS(2);

    private final int value;

    MobileDeviceType(int value) {
        this.value = value;
    }

    public static MobileDeviceType from(Integer value){
        for (MobileDeviceType type : MobileDeviceType.values()) {
            if (Objects.equal(type.value, value)) {
                return type;
            }
        }
        return null;
    }

    public final int value() {
        return value;
    }
}
