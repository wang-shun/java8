package io.terminus.doctor.event.dto.report.daily;

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

    /**
     * 后备
     */
    private int houbei;

    /**
     * 妊娠检查阴性
     */
    private int pregCheckResultYing;

    /**
     * 断奶
     */
    private int duannai;

    /**
     * 返情
     */
    private int fanqing;

    /**
     * 流产
     */
    private int liuchan;
}
