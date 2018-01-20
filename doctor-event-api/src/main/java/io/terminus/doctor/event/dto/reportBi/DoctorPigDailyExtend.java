package io.terminus.doctor.event.dto.reportBi;

import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.Data;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
@Data
public class DoctorPigDailyExtend extends DoctorPigDaily {
    private static final long serialVersionUID = -599859298774041243L;
    private Integer sowDailyPigCount;
    private Integer boarDailyPigCount;

    private Integer sowStart;
    private Integer sowEnd;
}
