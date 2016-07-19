package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDailyReportDto implements Serializable {
    private static final long serialVersionUID = -1731172501153113322L;

    /**
     *
     */
    private DoctorCheckPregDailyReport checkPreg;

    /**
     *
     */
    private DoctorDeadDailyReport dead;

    /**
     *
     */
    private DoctorDeliverDailyReport deliver;

    /**
     *
     */
    private DoctorLiveStockDailyReport liveStock;

    /**
     *
     */
    private DoctorMatingDailyReport mating;

    /**
     *
     */
    private DoctorSaleDailyReport sale;

    /**
     *
     */
    private DoctorWeanDailyReport wean;
}
