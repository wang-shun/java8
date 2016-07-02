package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 母猪事件触发的转入猪群事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorSowMoveInGroupInput extends DoctorMoveInGroupInput implements Serializable {
    private static final long serialVersionUID = -8922843195140861816L;

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
     * 仔猪转入猪舍id
     */
    @NotNull(message = "barnId.not.null")
    private Long toBarnId;

    /**
     * 仔猪转入猪舍名称
     */
    @NotEmpty(message = "barnName.not.empty")
    private String toBarnName;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    @NotNull(message = "pig.type.not.null")
    private Integer pigType;

    /**
     * 品系id
     */
    private Long geneticId;

    /**
     * 品系name
     */
    private String geneticName;
}
