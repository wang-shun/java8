package io.terminus.doctor.open.dto;

import io.terminus.doctor.event.dto.DoctorStatisticDto;
import io.terminus.doctor.user.model.DoctorOrg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: 公司概况(包含统计信息)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorOrgBasicDto implements Serializable {
    private static final long serialVersionUID = 2038546914711088035L;

    /**
     * 公司信息
     */
    private DoctorOrg org;

    /**
     * 公司统计信息
     */
    private DoctorStatisticDto orgStatistic;
}
