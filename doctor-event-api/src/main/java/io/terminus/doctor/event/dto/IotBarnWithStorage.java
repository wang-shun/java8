package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorBarn;
import lombok.Data;

/**
 * Created by xjn on 18/1/29.
 * email:xiaojiannan@terminus.io
 */
@Data
public class IotBarnWithStorage extends DoctorBarn {
    private static final long serialVersionUID = -6508505975277284896L;
    /**
     * 猪舍种猪数量
     */
    private Integer pigCount;

    /**
     * 猪舍仔猪数量
     */
    private Long pigGroupCount;
}
