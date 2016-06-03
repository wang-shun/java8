package io.terminus.doctor.event.event;

import io.terminus.doctor.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupCountEvent extends Event<Long> {

    /**
     * 公司id
     */
    private Long orgId;

    /**
     * 猪场id
     */
    private Long farmId;
}
