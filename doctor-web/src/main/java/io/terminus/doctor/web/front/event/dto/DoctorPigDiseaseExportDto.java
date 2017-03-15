package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigDiseaseExportDto implements Serializable{

    private static final long serialVersionUID = 4686393901501420403L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date diseaseDate;
    private String diseaseName;
    private String diseaseStaff;
    private String diseaseRemark;
    private String updatorName;
}
