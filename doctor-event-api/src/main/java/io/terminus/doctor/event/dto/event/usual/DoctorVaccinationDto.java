package io.terminus.doctor.event.dto.event.usual;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 免疫信息结果
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorVaccinationDto implements Serializable{

    private static final long serialVersionUID = 183960403500452272L;

    private Date vaccinationDate;

    private Long vaccinationId;

    private String vaccinationName;

    private Integer vaccinationResult;

    private Long vaccinationStaffId;

    private String vaccinationStaffName;

    private String vaccinationRemark;
}
