package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorLiveStockGroupEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = 3364720572200525585L;
}
