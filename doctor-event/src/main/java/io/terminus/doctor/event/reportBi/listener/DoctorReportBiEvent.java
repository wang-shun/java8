package io.terminus.doctor.event.reportBi.listener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorReportBiEvent {
    private Long orgId;
    private String orgName;
    private Long farmId;
    private String farmName;
    private Integer pigType;
    private Date sumAt;
}
