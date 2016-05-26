package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪只存栏事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorLiveStockGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = -7107436588688457949L;

    /**
     * 测量日期
     */
    private Date measureAt;
}
