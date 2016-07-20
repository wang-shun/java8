package io.terminus.doctor.event.report;

import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import lombok.Data;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe: pig 统计过滤器
 */
@Data
public class DoctorDailyPigCountChain {

    private final List<DoctorDailyEventCount> doctorDailyEventCounts;

    public DoctorDailyPigCountChain(List<DoctorDailyEventCount> doctorDailyEventCounts){
        this.doctorDailyEventCounts = doctorDailyEventCounts;
    }
}
