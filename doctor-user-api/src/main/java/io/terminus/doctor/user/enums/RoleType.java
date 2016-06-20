package io.terminus.doctor.user.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 用户角色类型, 用于前台登录后显示哪个首页
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public enum RoleType {

    ADMIN(0, "后台管理员"),
    MAIN(1, "主账号"),
    SUB_MULTI(2, "子账号-多个猪场"),
    SUB_SINGLE(3, "子账号-一个猪场"),
    MAIN_CLOSED(4, "主账号,未开通猪场软件");

    @Getter
    private final int value;
    @Getter
    private final String desc;

    RoleType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RoleType from(int number) {
        for (RoleType type : RoleType.values()) {
            if (Objects.equal(type.value, number)) {
                return type;
            }
        }
        return null;
    }
}
