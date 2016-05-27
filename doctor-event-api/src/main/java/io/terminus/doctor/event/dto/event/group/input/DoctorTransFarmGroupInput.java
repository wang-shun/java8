package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 转场事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTransFarmGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -2076403155703080256L;

    /**
     * 转场日期
     */
    @NotNull(message = "date.not.null")
    private String transGroupAt;

    /**
     * 转入猪场id
     */
    @NotNull(message = "to.farm.not.null")
    private Long toFarmId;

    @NotNull(message = "to.farm.not.null")
    private String toFarmName;

    /**
     * 转入猪舍id
     */
    @NotNull(message = "to.barn.not.null")
    private Long toBarnId;

    @NotNull(message = "to.barn.not.null")
    private String toBarnName;

    /**
     * 转入猪群
     */
    private Long toGroupId;

    /**
     * 转入猪群号
     */
    @NotNull(message = "to.group.not.null")
    private String toGroupCode;

    /**
     * 是否新建猪群 0:否 1:是
     * @see io.terminus.doctor.event.enums.IsCreateGroup
     */
    @NotNull(message = "is.create.group.not.null")
    private Integer isCreateGroup;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 猪只数 公 + 母的和
     */
    @NotNull(message = "quantity.not.null")
    private Integer quantity;

    @NotNull(message = "boarQty.not.null")
    private Integer boarQty;

    @NotNull(message = "boarQty.not.null")
    private Integer sowQty;

    /**
     * 总活体重(kg)
     */
    @NotNull(message = "weight.not.null")
    private Double weight;
}
