package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 销售日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorSaleDailyReport implements Serializable {
    private static final long serialVersionUID = -1170948612908091614L;

    private Integer boar;

    private Integer sow;

    private Integer nursery;

    private Integer fatten;
}
