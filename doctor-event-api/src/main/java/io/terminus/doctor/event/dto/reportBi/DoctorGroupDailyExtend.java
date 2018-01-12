package io.terminus.doctor.event.dto.reportBi;

import io.terminus.doctor.event.model.DoctorGroupDaily;
import lombok.Data;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
@Data
public class DoctorGroupDailyExtend extends DoctorGroupDaily{
    private static final long serialVersionUID = 4698248782160808926L;
    private Double dailyLivestockOnHand;
}
