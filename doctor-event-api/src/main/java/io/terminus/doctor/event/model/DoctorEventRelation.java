package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/3/13.
 * 事件关联关系表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEventRelation implements Serializable{
    private static final long serialVersionUID = 4693128842345088296L;

    private Long id;

    /**
     * 原猪事件id
     */
    private Long originPigEventId;

    /**
     * 原猪群事件id
     */
    private Long originGroupEventId;

    /**
     * 触发猪事件id
     */
    private Long triggerPigEventId;

    /**
     * 触发猪群事件
     */
    private Long triggerGroupEventId;

    /**
     * 状态
     * @see Status
     */
    private Integer status;

    private Date createdAt;

    private Date updatedAt;

    public enum Status{
        VALID(1, "有效"),
        HANDLING(0, "正在处理"),
        INVALID(-1, "无效");

        Status(Integer value, String name) {
            this.value = value;
            this.name = name;
        }
        @Getter
        Integer value;

        @Getter
        String name;

        public Status from(Integer value) {
            for (Status status : Status.values()) {
                if (status.getValue().equals(value)) {
                    return status;
                }
            }
            return null;
        }

    }
}
