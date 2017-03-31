package io.terminus.doctor.move.dto;

import io.terminus.doctor.user.model.DoctorFarm;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by xjn on 17/3/31.
 */
@Data
@AllArgsConstructor
public class DoctorFarmWithMobile {
    private DoctorFarm doctorFarm;
    private String mobile;
}
