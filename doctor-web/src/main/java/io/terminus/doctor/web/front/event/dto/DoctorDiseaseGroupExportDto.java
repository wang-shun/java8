package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorDiseaseGroupExportDto extends DoctorGroupEvent{


    /**
     * 疾病id
     */
    private Long diseaseId;

    /**
     * 疾病名称
     */
    private String diseaseName;

    /**
     * 诊断人员id
     */
    private Long doctorId;

    /**
     * 诊断人员name
     */
    private String doctorName;


    /**
     * 疾病猪只数
     */
    private Integer quantity;
}
