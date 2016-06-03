package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;

/**
 * Desc: 猪只数统计表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */

public interface DoctorPigTypeStatisticWriteService {

    /**
     * 创建DoctorPigTypeStatistic
     * @param pigTypeStatistic 猪只数统计表实例
     * @return 主键id
     */
    Response<Long> createPigTypeStatistic(DoctorPigTypeStatistic pigTypeStatistic);

    /**
     * 更新DoctorPigTypeStatistic
     * @param pigTypeStatistic 猪只数统计表实例
     * @return 是否成功
     */
    Response<Boolean> updatePigTypeStatisticById(DoctorPigTypeStatistic pigTypeStatistic);

    /**
     * 更新DoctorPigTypeStatistic
     * @param pigTypeStatistic 猪只数统计表实例
     * @return 是否成功
     */
    Response<Boolean> updatePigTypeStatisticByFarmId(DoctorPigTypeStatistic pigTypeStatistic);

    /**
     * 根据主键id删除DoctorPigTypeStatistic
     * @param pigTypeStatisticId 猪只数统计表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deletePigTypeStatisticById(Long pigTypeStatisticId);
}