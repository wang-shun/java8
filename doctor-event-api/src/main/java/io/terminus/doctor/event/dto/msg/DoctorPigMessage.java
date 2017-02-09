package io.terminus.doctor.event.dto.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 每只猪的消息提醒内容
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorPigMessage implements Serializable {
    private static final long serialVersionUID = 2878958119770364775L;

    /**
     *
     */
    private Integer messageCategory;

    /**
     * 消息描述
     */
    private String messageDescribe;

    private Long pigId;

    /**
     * 提醒动作类型
     */
    private Integer eventType;
    private String eventTypeName;

    /**
     * 距离需要执行事件的天数
     */
    private Integer timeDiff;

    /**
     * 猪只当前的状态
     */
    private Integer status;

}
