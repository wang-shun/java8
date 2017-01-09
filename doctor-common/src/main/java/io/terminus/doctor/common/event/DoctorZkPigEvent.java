package io.terminus.doctor.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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

    private static final long serialVersionUID = 1366282286841197089L;
    //private Map<String, Object> context;
    private Long pigId;
    private Long eventId;
    private Integer eventType;
}
