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

    private Date vaccinationDate; // 免疫日期

    private Long vaccinationId; // 疫苗名称Id

    private String vaccinationName; // 疫苗名称

    private Integer vaccinationResult;  // 防疫结果

    private Long vaccinationStaffId;    // 防疫人员Id

    private String vaccinationStaffName;    //防疫人员名称

    private String vaccinationRemark;   // 防疫注解
}
