package io.terminus.doctor.event.daily;

import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorPigEvent;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 对应不同事件统计方式
 */
public interface DoctorDailyEventCount {

    /**
     * 事件日报统计预处理信息
     * @param t 对应的事件
     * @return 返回对应的要处理的事件信息
     */
    boolean preDailyEventHandleValidate(DoctorPigEvent t);

    /**
     * 处理对应的事件信息
     * @param pigEvent 要处理的事件信息
     * @param dailyReportDto 对应的影响的结果信息
     */
    void dailyEventHandle(DoctorPigEvent pigEvent, DoctorDailyReportDto dailyReportDto);
}
