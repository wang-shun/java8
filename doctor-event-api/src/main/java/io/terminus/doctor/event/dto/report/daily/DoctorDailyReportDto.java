package io.terminus.doctor.event.dto.report.daily;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupStock;
import lombok.Data;

import javax.print.Doc;
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

    DoctorDailyReport dailyReport;  //猪日报

    DoctorGroupStock groupStock;    //猪群存栏
    /**
     * 是否失败, true 失败
     */
    private boolean fail;
}
