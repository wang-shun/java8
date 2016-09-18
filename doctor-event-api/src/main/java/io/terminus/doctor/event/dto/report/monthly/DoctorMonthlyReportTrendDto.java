package io.terminus.doctor.event.dto.report.monthly;

import io.terminus.doctor.event.model.DoctorParityMonthlyReport;
import lombok.AllArgsConstructor;
import lombok.Data;

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
public class DoctorMonthlyReportTrendDto implements Serializable {
    private static final long serialVersionUID = -135956012454392909L;

    private DoctorMonthlyReportDto report;          //查询当月统计

    private List<DoctorMonthlyReportDto> reports;   //当月开始往前推的统计

    private List<DoctorParityMonthlyReport> parityReports; //当月胎次产仔分析
}
