package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 批量事件时，发布zk事件需要的数据
 * xjn
 * 2017.1.10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgGroupPublishDto implements Serializable {
    private static final long serialVersionUID = 2917349097446538641L;

    private Long groupId;

    private Long eventId;

    private Date eventAt;

    private Integer eventType;
}
