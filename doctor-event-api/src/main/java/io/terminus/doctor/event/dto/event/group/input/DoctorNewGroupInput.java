package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 新建猪群所需字段
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorNewGroupInput extends BaseGroupInput implements Serializable {
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
     * 猪舍名称
     */
    @NotEmpty(message = "barnName.not.empty")
    private String barnName;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    @NotNull(message = "pig.type.not.null")
    private Integer pigType;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    @NotNull(message = "sex.not.null")
    private Integer sex;

    /**
     * 品种id
     */
    private Long breedId;

    /**
     * 品种name
     */
    private String breedName;

    /**
     * 品系id
     */
    private Long geneticId;

    /**
     * 品系name
     */
    private String geneticName;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    @NotNull(message = "source.not.null")
    private Integer source;
}
