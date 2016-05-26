package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Desc: 猪群事件类型枚举
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */

public enum GroupEventType {

    NEW(1, "新建猪群"),
    TRANS_IN(2, "转入猪群"),
    CHANGE(3, "猪群变动"),
    TRANS_GROUP(4, "猪群转群"),
    TURN_SEED(5, "商品猪转为种猪"),
    LIVE_STOCK(6, "猪只存栏"),
    DISEASE(7, "疾病"),
    ANTIEPIDEMIC(8, "防疫"),
    TRANS_FARM(9, "转场"),
    CLOSE(10, "关闭猪群");

    @Getter
    private final int value;
    @Getter
    private final String desc;

    GroupEventType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static GroupEventType from(int number) {
        for (GroupEventType type : GroupEventType.values()) {
            if (Objects.equal(type.value, number)) {
                return type;
            }
        }
        return null;
    }
}
