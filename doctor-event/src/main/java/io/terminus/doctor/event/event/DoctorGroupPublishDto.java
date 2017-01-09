package io.terminus.doctor.event.event;

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
public class DoctorGroupPublishDto implements Serializable {
    private static final long serialVersionUID = 2917349097446538641L;

    private Long groupId;

    private Long eventId;

    private Date eventAt;

    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;
}
