package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by xjn on 17/9/28.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarmEarlyEventAtDto {
    private Long farmId;
    private Date eventAt;
}
