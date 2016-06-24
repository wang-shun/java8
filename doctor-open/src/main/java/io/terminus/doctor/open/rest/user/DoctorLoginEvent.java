package io.terminus.doctor.open.rest.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorLoginEvent {

    String sessionId;
}
