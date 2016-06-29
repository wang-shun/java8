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
 * Descirbe: 疾病事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDiseaseDto implements Serializable{

    private static final long serialVersionUID = 7557956788545103933L;

    private Date diseaseDate;   // 疾病日期

    private Long diseaseId; // 疾病Id

    private String diseaseName; // 疾病名称

    private String diseaseStaff;    // 疾病操作人员

    private String diseaseRemark;   // 疾病注解
}
