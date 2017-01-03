package io.terminus.doctor.open.dto;

import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/22.
 * 日报外部调用封装
 */
@Data
public class DoctorDailyReportOpen implements Serializable{
    private static final long serialVersionUID = 5134718595331440935L;

    /**
     * 猪场名称
     */
    private String farmName;

    /**
     * 猪博士使用异常
     */
    private String farmStatus; //// TODO: 16/12/23 缺少

    /**
     * 统计时间
     */
    private Date sumAt;

    /**
     * 断奶仔猪日报
     */
    private DoctorWeanDailyReportOpen wean = new DoctorWeanDailyReportOpen();

    /**
     * 分娩日报
     */
    private DoctorDeliverDailyReportOpen deliver = new DoctorDeliverDailyReportOpen();

    /**
     * 妊娠检查日报
     */
    private DoctorCheckPregDailyReport checkPreg = new DoctorCheckPregDailyReport();

    /**
     * 配种日报
     */
    private DoctorMatingDailyReportOpen mating = new DoctorMatingDailyReportOpen();

    /**
     * 存栏日报
     */
    private DoctorLiveStockDailyReportOpen liveStock = new DoctorLiveStockDailyReportOpen();

    /**
     * 死淘日报
     */
    private DoctorDeadDailyReportOpen dead = new DoctorDeadDailyReportOpen();

    /**
     * 销售日报
     */
    private DoctorSaleDailyReportOpen sale = new DoctorSaleDailyReportOpen();

    /**
     * 采购金额
     */
    private double purchaseAmout; //// TODO: 16/12/23 缺少


}
