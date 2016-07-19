package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 死淘日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDeadDailyReport implements Serializable {
    private static final long serialVersionUID = 4988732219317374200L;

    private Integer boar;

    private Integer sow;

    private Integer farrow;

    private Integer nursery;

    private Integer fatten;
}
