package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc: 疾病事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorDiseaseGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = -697712004465910770L;

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
