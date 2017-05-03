package io.terminus.doctor.event.dto.report.daily;

import io.terminus.doctor.event.model.DoctorGroupChangeSum;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 日报统计dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDailyReportDto implements Serializable {
    private static final long serialVersionUID = -1731172501153113322L;

    DoctorDailyReport dailyReport;  //猪日报

    DoctorGroupChangeSum groupChangeSum; //猪群日存栏变化

    private Integer FattenWillOut;   //待出栏育肥数

    /**
     * 是否失败, true 失败
     */
    private boolean fail;
}
