package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorPigVaccinationExportDto implements Serializable{
    private static final long serialVersionUID = 7183585916208211988L;

    private String pigCode;
    private String barnName;
    private Date vaccinationDate;
    private String vaccinationItemName;
    private String vaccinationName;
    private String vaccinationStaffName;
    private String remark;
    private String updatorName;
}
