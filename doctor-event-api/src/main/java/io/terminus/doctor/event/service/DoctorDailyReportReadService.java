package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪场日报表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */

public interface DoctorDailyReportReadService {

    /**
     * 根据farmId和日期查询猪场日报表(缓存方式)
     * @param farmId 猪场id
     * @param sumAt  统计日期
     * @return 猪场日报表
     */
    Response<DoctorDailyReportDto> findDailyReportByFarmIdAndSumAtWithCache(@NotNull(message = "farmId.not.null") Long farmId,
                                                                            @NotEmpty(message = "date.not.null") String sumAt);

    /**
     * 根据日期初始化日报统计(job 和 event模块启动init使用)
     * @param date 日期
     * @return 日报统计list
     */
    Response<List<DoctorDailyReportDto>> initDailyReportByDate(@NotNull(message = "date.not.null") Date date);

    /**
     * 根据日期和猪场id获取初始化的日报统计
     * @param farmId 猪场id
     * @param date 日期
     * @return 日报统计
     */
    Response<DoctorDailyReportDto> initDailyReportByFarmIdAndDate(@NotNull(message = "farmId.not.null") Long farmId,
                                                                  @NotNull(message = "date.not.null") Date date);

    /**
     * 清理全部的日报缓存
     * @return 是否成功
     */
    Response<Boolean> clearAllReportCache();

    /**
     * 根据查询查询日报
     * @param date 日期
     * @return 日报list
     */
    Response<List<DoctorDailyReport>> findDailyReportBySumAt(@NotNull(message = "date.not.null") Date date);

    /**
     * 从redis查询所有需要更新的日报
     * @return map 的 key 为 farmId, value 为开始日期
     */
    Response<Map<Long, String>> getDailyReport2Update();
}
