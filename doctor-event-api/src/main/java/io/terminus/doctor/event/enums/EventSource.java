package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Created by xjn on 17/3/28.
 * 事件来源
 */
public enum EventSource {
    INPUT(1, "软件录入"),
    IMPORT(2, "导入数据"),
    MOVE(3, "迁移数据");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    EventSource(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static EventSource from(Integer value) {
        for (EventSource es : EventSource.values()) {
            if (Objects.equal(value, es.getValue())) {
                return es;
            }
        }
        return null;
    }
}
