package io.terminus.doctor.common.enums;

/**
 * @author Effet
 */
public enum UserStatus {

    /**
     * 已删除
     */
    DELETED(-3),

    /**
     * 已冻结
     */
    FROZEN(-2),

    /**
     * 已锁定
     */
    LOCKED(-1),

    /**
     * 未激活
     */
    NOT_ACTIVATE(0),

    /**
     * 正常
     */
    NORMAL(1);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public final int value() {
        return value;
    }
}
