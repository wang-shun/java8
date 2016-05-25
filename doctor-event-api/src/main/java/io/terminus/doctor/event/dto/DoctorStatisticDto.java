package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪的统计信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
public class DoctorStatisticDto implements Serializable {
    private static final long serialVersionUID = 4470661541591381247L;

    /**
     * 母猪存栏量
     */
    private Integer sow;

    /**
     * 产房仔猪存栏量
     */
    private Integer farrowPiglet;

    /**
     * 保育猪存栏量
     */
    private Integer nurseryPiglet;

    /**
     * 育肥猪存栏量
     */
    private Integer fattenPig;

    /**
     * 育种猪存栏量
     */
    private Integer breedingPig;
}
