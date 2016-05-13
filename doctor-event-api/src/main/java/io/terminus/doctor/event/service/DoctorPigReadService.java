package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorPigReadService {

    /**
     * 获取母猪数量
     * @param range
     * @param id
     * @return
     */
    Response<Integer> querySowPigCount(@NotNull(message = "input.range.empty")Integer range,
                                       @NotNull(message = "input.id.empty") Long id);

    /**
     * 获取公猪数量
     * @param range
     * @param id
     * @return
     */
    Response<Integer> queryBoarPigCount(@NotNull(message = "input.range.empty") Integer range,
                                        @NotNull(message = "input.id.empty") Long id);

    /**
     * 获取猪场Breed 状态存栏信息
     * @param farmId
     * @return
     */
    Response<Map<Long,Integer>> queryPigCountByBreed(@NotNull(message = "input.farmId.empty") Long farmId);

    /**
     * 猪状态存栏结构
     * @param farmId
     * @return
     */
    Response<Map<Long, Integer>> queryPigCountByStatus(@NotNull(message = "input.farmId,empty") Long farmId);


}
