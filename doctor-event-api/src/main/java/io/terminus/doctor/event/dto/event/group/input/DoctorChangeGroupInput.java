package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc: 猪群变动事件录入信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorChangeGroupInput extends BaseGroupInput implements Serializable {
    private static final long serialVersionUID = -7231563115604588914L;
}
