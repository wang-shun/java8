package io.terminus.doctor.common.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc:
 * Mail: houly@terminus.io
 * author: Hou Luyao
 * Date: 15/9/2.
 */
public enum PigmallCacheMessage {
    USER_LEVEL_DELETE(1, "用户等级删除"),
    USER_LEVEL_UPDATE(2, "用户等级更新"),
    USER_LEVEL_CREATE(3, "用户等级创建"),
    MEMBER_ROLE_UPDATE(4, "会员角色更新"),
    BRAND_CATEGORY_UPDATE(5, "品牌分类更新"),
    REGISTER_FROM_PIGDOCTOR(6, "用户在猪场软件注册");

    @Getter
    private long value;
    @Getter
    private String desc;

    private PigmallCacheMessage(long value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static PigmallCacheMessage from(long value) {
        for (PigmallCacheMessage status : PigmallCacheMessage.values()) {
            if (Objects.equal(status.value, value)) {
                return status;
            }
        }
        return null;
    }
}
