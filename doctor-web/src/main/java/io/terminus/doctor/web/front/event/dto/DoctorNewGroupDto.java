package io.terminus.doctor.web.front.event.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 新建猪群所需字段
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
public class DoctorNewGroupDto implements Serializable {
    private static final long serialVersionUID = 5586688187641324955L;

    /**
     * 猪场id
     */
    @NotNull(message = "farmId.not.null")
    private Long farmId;

    /**
     * 猪群号
     */
    @NotEmpty(message = "groupCode.not.empty")
    private String groupCode;

    /**
     * 猪舍id
     */
    @NotNull(message = "barnId.not.null")
    private Long barnId;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    @NotNull(message = "pig.type.not.null")
    private Integer pigType;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroup.Sex
     */
    @NotNull(message = "sex.not.null")
    private Integer sex;

    /**
     * 品种id
     */
    @NotNull(message = "breedId.not.null")
    private Long breedId;

    /**
     * 品系id
     */
    @NotNull(message = "geneticId.not.null")
    private Long geneticId;

    /**
     * 工作人员id
     */
    @NotNull(message = "staffId.not.null")
    private Long staffId;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    @NotNull(message = "source.not.null")
    private Integer source;

    /**
     * 事件猪只数
     */
    @NotNull(message = "quantity.not.null")
    private Integer quantity;

    /**
     * 总活体重(公斤)
     */
    @NotNull(message = "weight.not.null")
    private Double weight;

    /**
     * 平均日龄
     */
    @NotNull(message = "avgDayAge.not.null")
    private Double avgDayAge;

    /**
     * 备注
     */
    private String remark;
}
