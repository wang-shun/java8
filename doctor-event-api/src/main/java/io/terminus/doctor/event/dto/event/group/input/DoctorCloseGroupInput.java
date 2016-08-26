package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public Map<String, String> descMap() {
        return new HashMap<>();
    }
}
