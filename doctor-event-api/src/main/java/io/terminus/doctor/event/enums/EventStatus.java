package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Created by xjn on 17/3/9.
 * 事件状态
 */
public enum EventStatus {
    VALID(1, "有效"),
    HANDLING(0, "正在处理"),
    INVALID(-1, "无效");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    EventStatus(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static EventStatus from(Integer value) {
        for (EventStatus es : EventStatus.values()) {
            if (Objects.equal(value, es.getValue())) {
                return es;
            }
        }
        return null;
    }
}
