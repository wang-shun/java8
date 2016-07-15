package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 商品猪转为种猪事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTurnSeedGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -2955174319148999586L;

    /**
     * 转种猪后的id
     */
    @NotNull(message = "pig.id.not.null")
    private Long pigId;

    /**
     * 转种猪后的猪号
     */
    @NotNull(message = "pig.code.not.null")
    private String pigCode;

    /**
     * 母亲猪号
     */
    private String motherPigCode;

    /**
     * 转入日期
     */
    @NotNull(message = "date.not.null")
    private String transInAt;

    /**
     * 出生日期
     */
    @NotNull(message = "birthdate.not.null")
    private String birthDate;

    /**
     * 性别 0:种母猪 1:种公猪(ESex)
     * @see io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent.Sex
     */
    @NotNull(message = "sex.not.null")
    private Integer sex;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    private Long toBarnId;

    private String toBarnName;

    @Min(value = 0, message = "weight.gt.0")
    private Double weight;
}
