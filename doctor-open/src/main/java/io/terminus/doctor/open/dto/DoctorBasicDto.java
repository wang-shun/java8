package io.terminus.doctor.open.dto;

import io.terminus.doctor.user.model.DoctorOrg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBasicDto implements Serializable {
    private static final long serialVersionUID = 7618953726708026762L;

    /**
     * 公司信息
     */
    private DoctorOrg org;

    /**
     * 公司统计信息
     */
    private List<DoctorStatisticDto> orgStatistics;

    /**
     * 猪场概况
     */
    private List<DoctorFarmBasicDto> farmsBasic;
}
