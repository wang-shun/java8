package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 日报统计dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDailyReportDto implements Serializable {
    private static final long serialVersionUID = -1731172501153113322L;

    /**
     * 妊娠检查日报
     */
    private DoctorCheckPregDailyReport checkPreg;

    /**
     * 死淘日报
     */
    private DoctorDeadDailyReport dead;

    /**
     * 分娩日报
     */
    private DoctorDeliverDailyReport deliver;

    /**
     * 存栏日报
     */
    private DoctorLiveStockDailyReport liveStock;

    /**
     * 配种日报
     */
    private DoctorMatingDailyReport mating;

    /**
     * 销售日报
     */
    private DoctorSaleDailyReport sale;

    /**
     * 断奶仔猪日报
     */
    private DoctorWeanDailyReport wean;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 统计时间
     */
    private Date sumAt;
}
