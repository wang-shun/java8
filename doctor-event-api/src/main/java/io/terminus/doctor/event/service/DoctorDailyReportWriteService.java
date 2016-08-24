package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪场日报表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */

public interface DoctorDailyReportWriteService {

    /**
     * job批量创建DoctorDailyReport(先删除, 再创建)
     * @param farmIds 猪场ids
     * @param sumAt 统计时间
     * @return 是否成功
     */
    Response<Boolean> createDailyReports(@NotNull(message = "farmId.not.null") List<Long> farmIds,
                                         @NotNull(message = "date.not.null") Date sumAt);
}