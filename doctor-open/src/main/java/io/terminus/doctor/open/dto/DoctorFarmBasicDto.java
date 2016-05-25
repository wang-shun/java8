package io.terminus.doctor.open.dto;

import io.terminus.doctor.user.model.DoctorFarm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪场基本概况
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarmBasicDto implements Serializable {
    private static final long serialVersionUID = -937658617269265838L;

    /**
     * 猪场信息
     */
    private DoctorFarm farm;

    /**
     * 猪场统计信息
     */
    private List<DoctorStatisticDto> farmStatistics;
}
