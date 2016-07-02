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
    private Integer breedId;

    private String breedName;

    private Integer boarQty;

    private Integer sowQty;

    /**
     * 总活体重(单位:kg)
     */
    private Double weight;

    /**
     * 单价(分)
     */
    private Long price;

    /**
     * 金额(分)
     */
    private Long amount;

    /**
     * 客户id
     */
    private Long customerId;

    private String customerName;
}