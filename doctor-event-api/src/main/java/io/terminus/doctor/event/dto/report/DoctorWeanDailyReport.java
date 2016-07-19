package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 断奶仔猪日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorWeanDailyReport implements Serializable {
    private static final long serialVersionUID = 5413097064736038911L;

    private Integer count;

    private Double weight;
}
