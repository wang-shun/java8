package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 转入猪群事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorMoveInGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -2457482464847828070L;

    /**
     * 转入日期
     */
    @NotNull(message = "date.not.null")
    private String moveInAt;

    /**
     * 猪群转移类型
     * @see io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent.InType
     */
    @NotNull(message = "inType.not.null")
    private Integer inType;

    /**
     * 猪群转移类型名
     */
    @NotEmpty(message = "inType.not.null")
    private String inTypeName;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    @NotNull(message = "source.not.null")
    private Integer source;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroup.Sex
     */
    @NotNull(message = "sex.not.null")
    private Integer sex;

    private Integer breedId;

    private String breedName;

    private Long fromBarnId;

    private String fromBarnName;

    private Long toBarnId;

    private String toBarnName;

    private Long fromGroupId;

    private String fromGroupCode;

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
     * 平均日龄
     */
    @NotNull(message = "avgDayAge.not.null")
    private Double avgDayAge;

    /**
     * 平均体重(单位:kg)
     */
    @NotNull(message = "avgWeight.not.null")
    private Double avgWeight;

    /**
     * 总价值(分)
     */
    private Long amount;
}
