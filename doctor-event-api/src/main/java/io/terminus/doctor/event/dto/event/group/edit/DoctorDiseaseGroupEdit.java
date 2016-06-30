package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorDiseaseGroupEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = -3643661984731724628L;

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
}
