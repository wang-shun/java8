package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorVaccinationGroupExportDto extends DoctorGroupEvent{
    /**
     *  疫苗id
     */
    private Long vaccinId;

    /**
     *  疫苗名称
     */
    private String vaccinName;

    /**
     *  防疫结果: 1:阳性 -1:阴性
     *  @see io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupInput.VaccinResult
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
