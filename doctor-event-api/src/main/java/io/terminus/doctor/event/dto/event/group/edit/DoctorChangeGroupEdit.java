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
public class DoctorChangeGroupEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = -2361907495360533710L;

    /**
     * 变动原因id
     */
    private Long changeReasonId;

    /**
     * 变动原因
     */
    private String changeReasonName;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    @NotNull(message = "boarQty.not.null")
    private Integer boarQty;

    @NotNull(message = "boarQty.not.null")
    private Integer sowQty;

    /**
     * 总活体重(单位:kg)
     */
    @NotNull(message = "weight.not.null")
    private Double weight;

    /**
     * 单价(分)
     */
    @Min(value = 0L, message = "price.gt.0")
    private Long price;

    /**
     * 金额(分)
     */
    @Min(value = 0L, message = "amount.gt.0")
    private Long amount;

    /**
     * 客户id
     */
    private Long customerId;

    private String customerName;
}