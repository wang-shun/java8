package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/3/9.
 * 事件请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEventModifyRequest implements Serializable{

    private static final long serialVersionUID = -1880711380492734949L;

    private Long id;

    /**
     * 猪场ID
     */
    private Long farmId;

    /**
     * 目标id
     */
    private Long businessId;

    /**
     * 目标code
     */
    private String businessCode;

    /**
     * 上一个事件
     */
    private Long startEventId;

    /**
     * 处理的事件ID
     */
    private Long eventId;

    /**
     * 事件目标类型
     * @see TYPE
     */
    private Integer type;
    /**
     *  事件Json
     *  @see DoctorPigEvent
     */
    private String content;

    /**
     * 请求状态
     * @see io.terminus.doctor.event.enums.EventRequestStatus
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 当不知道具体错误原因时记录错误堆栈
     */
    private String errorStack;

    /**
     * 操作人id
     */
    private Long userId;

    /**
     * 操作人真实姓名
     */
    private String userName;

    private Date createdAt;

    private Date updatedAt;

    public enum TYPE{
        PIG(1, "猪"),
        GROUP(2, "猪群");

        TYPE(Integer value, String name) {
            this.value = value;
            this.name = name;
        }
        @Getter
        Integer value;

        @Getter
        String name;

        public TYPE from(Integer value) {
            for (TYPE type : TYPE.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            return null;
        }

    }
}
