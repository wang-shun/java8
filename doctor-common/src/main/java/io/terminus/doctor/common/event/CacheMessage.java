package io.terminus.doctor.common.event;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc:
 * author: 陈增辉
 * Date: 2016年06月14日
 */
public enum CacheMessage {
    USER_DATA_PERMISSION(1, "用户数据权限更新");

    @Getter
    private long value;
    @Getter
    private String desc;

    private CacheMessage(long value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static CacheMessage from(long value) {
        for (CacheMessage status : CacheMessage.values()) {
            if (Objects.equal(status.value, value)) {
                return status;
            }
        }
        return null;
    }
}
