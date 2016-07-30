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
public class DoctorAntiepidemicGroupEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = 2797308035172353133L;

    /**
     *  防疫结果: 1:阳性 -1:阴性
     *  @see io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent.VaccinResult
     */
    private Integer vaccinResult;

    /**
     * 防疫项目id
     */
    private Long vaccinItemId;

    /**
     * 防疫项目名称
     */
    private String vaccinItemName;

    /**
     *  防疫人员id
     */
    private Long vaccinStaffId;

    /**
     *  防疫人员名称
     */
    private String vaccinStaffName;
}
