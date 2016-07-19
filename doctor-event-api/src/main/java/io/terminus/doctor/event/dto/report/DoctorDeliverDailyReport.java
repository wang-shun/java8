package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 分娩日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDeliverDailyReport implements Serializable {
    private static final long serialVersionUID = 2753759652989219311L;

    private Integer nest;

    private Integer live;

    private Integer health;

    private Integer weak;

    private Integer black;
}
