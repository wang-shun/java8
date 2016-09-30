package io.terminus.doctor.event.dto.report.daily;

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

    /**
     * 公猪
     */
    private int boar;

    /**
     * 母猪
     */
    private int sow;

    /**
     * 产房仔猪
     */
    private int farrow;

    /**
     * 保育猪
     */
    private int nursery;

    /**
     * 育肥猪
     */
    private int fatten;

    /**
     * 后备猪
     */
    private int houbei;

    public void addSowBoar(DoctorDeadDailyReport doctorDeadDailyReport){
        this.boar += doctorDeadDailyReport.getBoar();
        this.sow += doctorDeadDailyReport.getSow();
    }
}
