package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 配种日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorMatingDailyReport implements Serializable {
    private static final long serialVersionUID = -7708318908444027462L;

    private Integer houbei;

    private Integer duannai;

    private Integer fanqing;

    private Integer liuchan;
}
