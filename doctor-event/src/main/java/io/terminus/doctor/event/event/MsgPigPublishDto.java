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
 * Date: 2017/1/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgPigPublishDto implements Serializable {
    private static final long serialVersionUID = -3312764468271438715L;

    private Long pigId;

    private Long eventId;

    private Date eventAt;

    private Integer eventType;
}
