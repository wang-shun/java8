package io.terminus.doctor.event.dto.search;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/4/12.
 */
@Data
public class DoctorGroupCountDto implements Serializable{
    private static final long serialVersionUID = 317216605312132405L;

    private Integer deliverPigCount;

    /**
     * 保育数量
     */
    private Integer nurseryPigletCount;

    private Integer reserveCount;

    private Integer fattenPigCount;
}
