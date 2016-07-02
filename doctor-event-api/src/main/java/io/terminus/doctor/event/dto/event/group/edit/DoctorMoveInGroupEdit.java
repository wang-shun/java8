package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorMoveInGroupEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = 4275736367348394074L;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private Integer source;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 平均体重(单位:kg)
     */
    private Double avgWeight;

    /**
     * 总价值(分)
     */
    private Long amount;
}
