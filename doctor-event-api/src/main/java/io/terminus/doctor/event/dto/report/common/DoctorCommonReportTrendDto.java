package io.terminus.doctor.event.dto.report.common;

import io.terminus.doctor.event.model.DoctorBoarMonthlyReport;
import io.terminus.doctor.event.model.DoctorParityMonthlyReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 月报趋势dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorCommonReportTrendDto implements Serializable {
    private static final long serialVersionUID = -135956012454392909L;

    private DoctorCommonReportDto report;          //查询当月统计

    private List<DoctorCommonReportDto> reports;   //当月开始往前推的统计

    private List<DoctorParityMonthlyReport> parityReports; //当月胎次产仔分析

    private List<DoctorBoarMonthlyReport> boarReports;//公猪生产成绩月报
}
