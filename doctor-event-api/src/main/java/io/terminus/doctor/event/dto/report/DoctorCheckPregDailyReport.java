package io.terminus.doctor.event.dto.report;

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

    private Integer positive;

    private Integer negative;

    private Integer duannai;

    private Integer fanqing;
}
