package io.terminus.doctor.event.dto.report.daily;

import com.google.common.collect.Maps;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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
    private DoctorCheckPregDailyReport checkPreg = new DoctorCheckPregDailyReport();

    /**
     * 死淘日报
     */
    private DoctorDeadDailyReport dead = new DoctorDeadDailyReport();

    /**
     * 分娩日报
     */
    private DoctorDeliverDailyReport deliver = new DoctorDeliverDailyReport();

    /**
     * 存栏日报
     */
    private DoctorLiveStockDailyReport liveStock = new DoctorLiveStockDailyReport();

    /**
     * 配种日报
     */
    private DoctorMatingDailyReport mating = new DoctorMatingDailyReport();

    /**
     * 销售日报
     */
    private DoctorSaleDailyReport sale = new DoctorSaleDailyReport();

    /**
     * 断奶仔猪日报
     */
    private DoctorWeanDailyReport wean = new DoctorWeanDailyReport();

    /**
     * 每个猪群的存栏
     */
    private Map<Long, Integer> groupCountMap = Maps.newHashMap();

    /**
     * 母猪存栏
     */
    private int sowCount;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 统计时间
     */
    private Date sumAt;

    /**
     * 是否失败, true 失败
     */
    private boolean fail;

    /**
     * 只修改猪群相关的统计
     */
    public void setGroup(DoctorDailyReportDto report) {
        //死淘
        this.dead.setFarrow(this.dead.getFarrow() + report.getDead().getFarrow());
        this.dead.setNursery(this.dead.getNursery() + report.getDead().getNursery());
        this.dead.setFatten(this.dead.getFatten() + report.getDead().getFatten());
        this.dead.setHoubei(this.dead.getHoubei() + report.getDead().getHoubei());

        //存栏
        this.liveStock.setFarrow(report.getLiveStock().getFarrow());
        this.liveStock.setNursery(report.getLiveStock().getNursery());
        this.liveStock.setFatten(report.getLiveStock().getFatten());
        this.liveStock.setHoubeiSow(report.getLiveStock().getHoubeiSow() == 0? this.liveStock.getHoubeiSow() : report.getLiveStock().getHoubeiSow());
        this.liveStock.setHoubeiBoar(report.getLiveStock().getHoubeiBoar() == 0? this.liveStock.getHoubeiBoar() : report.getLiveStock().getHoubeiBoar());

        //销售
        this.sale.setNursery(this.sale.getNursery() + report.getSale().getNursery());
        this.sale.setFatten(this.sale.getFatten() + report.getSale().getFatten());
        this.sale.setFattenPrice(report.getSale().getFattenPrice() == 0L? this.sale.getFattenPrice() : report.getSale().getFattenPrice());
        this.sale.setBasePrice10(report.getSale().getBasePrice10() == 0L? this.sale.getBasePrice10() : report.getSale().getBasePrice10());
        this.sale.setBasePrice15(report.getSale().getBasePrice15() == 0L? this.sale.getBasePrice15() : report.getSale().getBasePrice15());
    }
}
