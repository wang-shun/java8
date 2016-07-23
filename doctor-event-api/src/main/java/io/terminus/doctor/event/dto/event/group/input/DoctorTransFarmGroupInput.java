package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

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
public class DoctorTransFarmGroupInput extends DoctorTransGroupInput implements Serializable {
    private static final long serialVersionUID = -2076403155703080256L;

    /**
     * 转入猪场id
     */
    @NotNull(message = "to.farm.not.null")
    private Long toFarmId;

    @NotEmpty(message = "to.farm.not.null")
    private String toFarmName;
}
