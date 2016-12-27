package io.terminus.doctor.common.event;

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
public class DoctorZkGroupEvent implements Serializable {

    private static final long serialVersionUID = -6729506055005246412L;
    private Map<String, Object> context;
}
