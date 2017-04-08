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
     * 保育日报表
     */
    private DoctorNurseryReport nursery = new DoctorNurseryReport();

    /**
     * 育肥日报表
     */
    private DoctorFatteningReport fattening = new DoctorFatteningReport();
    /**
     * 后备日报表
     */
    private DoctorHoubeiReport houbei = new DoctorHoubeiReport();
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
}
