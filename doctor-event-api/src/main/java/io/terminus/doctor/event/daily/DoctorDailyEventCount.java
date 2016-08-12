package io.terminus.doctor.event.daily;

import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorPigEvent;

import java.util.List;
import java.util.Map;

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
    List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t);

    /**
     * 处理对应的事件信息
     * @param t 要处理的事件信息
     * @param context 对应的处理结果信息
     */
    /**
     * 处理对应的事件信息
     * @param t 要处理的事件信息
     * @param doctorDailyReportDto 对应的影响的结果信息
     * @param context 对应的处理结果信息
     */
    void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context);
}
