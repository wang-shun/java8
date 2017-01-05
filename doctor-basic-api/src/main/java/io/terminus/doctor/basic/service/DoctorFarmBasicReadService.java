package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorFarmBasic;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪场基础数据关联表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
public interface DoctorFarmBasicReadService {

    /**
     * 根据id查询猪场基础数据关联表
     * @param farmBasicId 主键id
     * @return 猪场基础数据关联表
     */
    Response<DoctorFarmBasic> findFarmBasicById(Long farmBasicId);

    /**
     * 根据farmId查询猪场基础数据关联表
     * @param farmId 猪场id
     * @return 猪场基础数据关联表
     */
    Response<DoctorFarmBasic> findFarmBasicByFarmId(@NotNull(message = "farmId.not.null") Long farmId);
}
