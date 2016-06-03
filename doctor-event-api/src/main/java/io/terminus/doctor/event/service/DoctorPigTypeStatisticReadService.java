package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪只数统计表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */

public interface DoctorPigTypeStatisticReadService {

    /**
     * 根据id查询猪只数统计表
     * @param pigTypeStatisticId 主键id
     * @return 猪只数统计表
     */
    Response<DoctorPigTypeStatistic> findPigTypeStatisticById(@NotNull(message = "pigTypeStatisticId.not.null") Long pigTypeStatisticId);

    /**
     * 根据farmId查询猪只数统计表
     * @param farmId 猪场id
     * @return 猪只数统计表
     */
    Response<DoctorPigTypeStatistic> findPigTypeStatisticByFarmId(@NotNull(message = "farmId.not.null") Long farmId);
}
