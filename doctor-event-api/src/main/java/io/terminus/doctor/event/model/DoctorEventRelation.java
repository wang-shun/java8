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
     * 原事件id
     */
    private Long originEventId;

    /**
     * 触发事件id
     */
    private Long triggerEventId;

    /**
     * 触发的事件的目标类型
     * @see TargetType
     */
    private Integer triggerTargetType;

    /**
     * 状态
     * @see Status
     */
    private Integer status;

    private Date createdAt;

    private Date updatedAt;

    public enum Status{
        VALID(1, "有效"),
        INVALID(0, "无效");

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

    public enum TargetType{
        PIG(1, "猪事件"),
        GROUP(2, "猪群事件");

        TargetType(Integer value, String name) {
            this.value = value;
            this.name = name;
        }
        @Getter
        Integer value;

        @Getter
        String name;

        public TargetType from(Integer value) {
            for (TargetType tt : TargetType.values()) {
                if (tt.getValue().equals(value)) {
                    return tt;
                }
            }
            return null;
        }

    }
}
