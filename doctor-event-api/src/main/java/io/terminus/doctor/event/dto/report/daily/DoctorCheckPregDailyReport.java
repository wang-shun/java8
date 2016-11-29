package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 妊娠检查日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorCheckPregDailyReport implements Serializable {
    private static final long serialVersionUID = 4570313490797897082L;

    /**
     * 妊娠检查阳性
     */
    private int positive;

    /**
     * 妊娠检查阴性
     */
    private int negative;

    /**
     * 返情
     */
    private int fanqing;

    /**
     * 流产
     */
    private int liuchan;
}
