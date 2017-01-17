package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;

import javax.validation.constraints.NotNull;

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
    Response<Boolean> updatePigTypeStatistic(DoctorPigTypeStatistic pigTypeStatistic);

    /**
     * 根据主键id删除DoctorPigTypeStatistic
     * @param pigTypeStatisticId 猪只数统计表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deletePigTypeStatisticById(Long pigTypeStatisticId);

    /**
     * 统计猪群数量
     * @param orgId  公司id
     * @param farmId 猪场id
     * @return 是否成功
     */
    Response<Boolean> statisticGroup(@NotNull(message = "org.id.not.null") Long orgId,
                                     @NotNull(message = "farm.id.not.null") Long farmId);

    /**
     * 统计猪数量
     * @param orgId  公司id
     * @param farmId 猪场id
     * @param pigType 猪类
     * @see DoctorPig.PigSex
     * @return 是否成功
     */
    Response<Boolean> statisticPig(@NotNull(message = "org.id.not.null") Long orgId,
                                   @NotNull(message = "farm.id.not.null") Long farmId,
                                   @NotNull(message = "pig.type.not.null") Integer pigType);
}