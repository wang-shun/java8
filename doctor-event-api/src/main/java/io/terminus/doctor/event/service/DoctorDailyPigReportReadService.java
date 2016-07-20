package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPigEvent;

import java.util.Date;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe: pig 相关的日报统计
 */
public interface DoctorDailyPigReportReadService {

    /**
     * 统计对应猪场日期类型数据
     * @param farmId
     * @param sumAt
     * @return
     */
    Response<DoctorDailyReportDto> countByFarmIdDate(Long farmId, Date sumAt);

    /**
     * 统计对应的Date 当前event 下包含的所有猪场信息
     * @param sumAt
     * @return
     */
    Response<List<DoctorDailyReportDto>> countByDate(Date sumAt);

    /**
     * 单个事件统计对应的结果信息
     * @param doctorPigEvent
     * @return
     */
    Response<DoctorDailyReportDto> countSinglePigEvent(DoctorPigEvent doctorPigEvent);
}
