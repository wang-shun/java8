package io.terminus.doctor.event.dto.search;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/4/12.
 */
@Data
public class DoctorPigCountDto implements Serializable {
    private static final long serialVersionUID = 3511970677906605391L;

    /**
     * 妊娠母猪数量
     */
    private Integer pregCount;

    /**
     * 空怀母猪数量
     */
    private Integer konghuaiCount;

    /**
     * 哺乳母猪数量
     */
    private Integer farrowCount;

    /**
     * 上面三个数量和
     */
    private Integer sowTotalCount;

    /**
     * 公猪数量
     */
    private Integer boarCount;
}
