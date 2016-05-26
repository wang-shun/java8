package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * Desc: 关闭猪群事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorCloseGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = 8337863112678158187L;

    /**
     * 关闭时间 yyyy-MM-dd
     */
    @NotEmpty(message = "date.not.null")
    private String closeAt;
}
