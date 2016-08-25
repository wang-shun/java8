package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

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
     * 转种猪后的猪号
     */
    @NotNull(message = "pig.code.not.null")
    private String pigCode;

    /**
     * 母亲猪 耳缺号
     */
    private String motherEarCode;

    /**
     * 耳缺号
     */
    private String earCode;

    /**
     * 出生日期
     */
    @NotNull(message = "birthdate.not.null")
    private String birthDate;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    /**
     * 转入猪舍
     */
    @NotNull(message = "barnId.not.null")
    private Long toBarnId;

    @NotEmpty(message = "barnId.not.null")
    private String toBarnName;

    /**
     * 重量(kg)
     */
    @Min(value = 0, message = "weight.gt.0")
    private Double weight;
}
