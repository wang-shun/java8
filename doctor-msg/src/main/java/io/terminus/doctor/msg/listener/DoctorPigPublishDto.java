package io.terminus.doctor.msg.listener;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 批量事件时，发布事件需要的数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/1/9
 */
@Data
public class DoctorPigPublishDto implements Serializable {
    private static final long serialVersionUID = -3312764468271438715L;

    private Long pigId;

    private Long eventId;

    private Date eventAt;

    private Integer eventType;
}
