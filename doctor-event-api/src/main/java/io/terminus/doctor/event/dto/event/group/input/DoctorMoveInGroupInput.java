package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private String moveInAt;


}
