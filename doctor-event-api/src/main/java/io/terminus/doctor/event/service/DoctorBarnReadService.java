package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorBarn;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 猪舍表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBarnReadService {

    /**
     * 根据id查询猪舍表
     * @param barnId 主键id
     * @return 猪舍表
     */
    Response<DoctorBarn> findBarnById(Long barnId);

    /**
     * 根据farmId查询猪舍表
     * @param farmId 猪场id
     * @return 猪舍表
     */
    Response<List<DoctorBarn>> findBarnsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 根据一些枚举条件查询猪舍
     * @param farmId  猪场id
     * @param pigType 猪类
     * @see io.terminus.doctor.common.enums.PigType
     * @param canOpenGroup 能否新建猪群
     * @see io.terminus.doctor.event.model.DoctorBarn.CanOpenGroup
     * @param status 猪舍状态
     * @see io.terminus.doctor.event.model.DoctorBarn.Status
     * @return 猪舍列表
     */
    Response<List<DoctorBarn>> findBarnsByEnums(@NotNull(message = "farmId.not.null") Long farmId,
                                                Integer pigType, Integer canOpenGroup, Integer status);

    /**
     * 查询当前猪舍的存栏量
     * @param barnId 猪舍id
     * @return 存栏量
     */
    Response<Integer> countPigByBarnId(@NotNull(message = "barnId.not.null") Long barnId);
}
