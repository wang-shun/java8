package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
    @NotNull(message = "source.not.null")
    private Integer source;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 平均体重(单位:kg)
     */
    @NotNull(message = "avgWeight.not.null")
    private Double avgWeight;

    /**
     * 总价值(分)
     */
    @Min(value = 0L, message = "amount.gt.0")
    private Long amount;
}
