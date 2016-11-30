package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorZkPigEvent implements Serializable {
    private static final long serialVersionUID = -3809912042345219678L;

    private Map<String, Object> context;
}
