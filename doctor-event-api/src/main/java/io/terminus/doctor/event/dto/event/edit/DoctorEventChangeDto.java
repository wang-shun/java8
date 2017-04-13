package io.terminus.doctor.event.dto.event.edit;

import lombok.Data;

/**
 * Created by xjn on 17/4/13.
 * 记录编辑的变化
 */
@Data
public class DoctorEventChangeDto {
    private Long farmId;

    private Long businessId;


}
