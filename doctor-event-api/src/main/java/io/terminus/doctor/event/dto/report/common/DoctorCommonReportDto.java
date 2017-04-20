package io.terminus.doctor.event.dto.report.common;

import io.terminus.doctor.event.model.DoctorBaseReport;
import io.terminus.doctor.event.model.DoctorIndicatorReport;
import io.terminus.doctor.event.model.DoctorRangeReport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪场报表json字段(月报，周报公用)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Data
public class DoctorCommonReportDto implements Serializable {
    private static final long serialVersionUID = -2490312543838256507L;

    /**
     * 是否失败, true 失败(作为区分 0 与 未查询到结果)
     */
    private boolean fail;

    private String date;                      //统计月份 2016年08月, 供前台显示 或 第几周

    private DoctorBaseReport changeReport;    //数量变化报表

    private DoctorRangeReport indicatorReport;  //指标

    /**
     * 胎次分布
     */
    private List<DoctorStockStructureCommonReport> parityStockList;

    /**
     * 品类分布
     */
    private List<DoctorStockStructureCommonReport> breedStockList;
}
