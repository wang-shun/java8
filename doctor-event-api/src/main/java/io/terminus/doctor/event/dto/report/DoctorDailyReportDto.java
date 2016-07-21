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
     * 只修改猪相关的统计
     */
    public void setPig(DoctorDailyReportDto report) {
        //妊娠检查
        this.checkPreg.setPositive(this.checkPreg.getPositive() + report.getCheckPreg().getPositive());
        this.checkPreg.setNegative(this.checkPreg.getNegative() + report.getCheckPreg().getNegative());
        this.checkPreg.setFanqing(this.checkPreg.getFanqing() + report.getCheckPreg().getFanqing());
        this.checkPreg.setLiuchan(this.checkPreg.getLiuchan() + report.getCheckPreg().getLiuchan());

        //死淘
        this.dead.setBoar(this.dead.getBoar() + report.getDead().getBoar());
        this.dead.setSow(this.dead.getSow() + report.getDead().getSow());

        //分娩
        this.deliver.setNest(this.deliver.getNest() + report.getDeliver().getNest());
        this.deliver.setLive(this.deliver.getLive() + report.getDeliver().getLive());
        this.deliver.setHealth(this.deliver.getHealth() + report.getDeliver().getHealth());
        this.deliver.setWeak(this.deliver.getWeak() + report.getDeliver().getWeak());
        this.deliver.setBlack(this.deliver.getBlack() + report.getDeliver().getBlack());

        //存栏
        this.liveStock.setBoar(report.getLiveStock().getBoar());
        this.liveStock.setHoubeiSow(report.getLiveStock().getHoubeiSow());
        this.liveStock.setPeihuaiSow(report.getLiveStock().getPeihuaiSow());
        this.liveStock.setBuruSow(report.getLiveStock().getBuruSow());
        this.liveStock.setKonghuaiSow(report.getLiveStock().getKonghuaiSow());

        //配种
        this.mating.setHoubei(this.mating.getHoubei() + report.getMating().getHoubei());
        this.mating.setDuannai(this.mating.getDuannai() + report.getMating().getDuannai());
        this.mating.setFanqing(this.mating.getFanqing() + report.getMating().getFanqing());
        this.mating.setLiuchan(this.mating.getLiuchan() + report.getMating().getLiuchan());

        //销售
        this.sale.setBoar(this.sale.getBoar() + report.getSale().getBoar());
        this.sale.setSow(this.sale.getSow() + report.getSale().getSow());

        //断奶仔猪
        this.wean.setCount(this.wean.getCount() + report.getWean().getCount());
        this.wean.setWeight(this.wean.getWeight() + report.getWean().getWeight());
    }

    /**
     * 只修改猪群相关的统计
     */
    public void setGroup(DoctorDailyReportDto report) {
        //死淘
        this.dead.setFarrow(this.dead.getFarrow() + report.getDead().getFarrow());
        this.dead.setNursery(this.dead.getNursery() + report.getDead().getNursery());
        this.dead.setFatten(this.dead.getFatten() + report.getDead().getFatten());

        //存栏
        this.liveStock.setFarrow(report.getLiveStock().getFarrow());
        this.liveStock.setNursery(report.getLiveStock().getNursery());
        this.liveStock.setFatten(report.getLiveStock().getFatten());

        //销售
        this.sale.setNursery(this.sale.getNursery() + report.getSale().getNursery());
        this.sale.setFatten(this.sale.getFatten() + report.getSale().getFatten());
    }
}
