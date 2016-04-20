package io.terminus.doctor.common.enums;

/**
 * 用户类型枚举
 *
 * BUYER / SELLER 归类为角色, 不属于用户类型
 *
 * @author Effet
 */
public enum UserType {

    /**
     * 超级管理员
     */
    ADMIN(1),

    /**
     * 普通用户
     */
    NORMAL(2),

    /**
     * 后台运营
     */
    OPERATOR(3),

    /**
     * 站点拥有者
     */
    SITE_OWNER(4);

    private final int value;

    UserType(int value) {
        this.value = value;
    }

    public final int value() {
        return value;
    }
}
