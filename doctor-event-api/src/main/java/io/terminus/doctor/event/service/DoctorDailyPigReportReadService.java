package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;

import java.util.Date;

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
}
