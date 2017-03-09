package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Created by xjn on 17/3/9.
 * 编辑事件请求的状态
 */
public enum EventRequestStatus {

    WAITING(0, "等待处理"),
    HANDLING(1, "处理中"),
    SUCCESS(2, "处理成功"),
    FAILED(-1, "处理失败");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    EventRequestStatus(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static EventRequestStatus from(Integer value) {
        for (EventRequestStatus ers : EventRequestStatus.values()) {
            if (Objects.equal(value, ers.getValue())) {
                return ers;
            }
        }
        return null;
    }
}
