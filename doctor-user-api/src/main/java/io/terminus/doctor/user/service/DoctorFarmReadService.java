package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;

import java.util.List;

/**
 * Desc: 猪场信息读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmReadService {

    /**
     * 根据猪场id查询猪场信息
     * @param farmId 猪场id
     * @return 猪场信息
     */
    Response<DoctorFarm> findFarmById(Long farmId);

    /**
     * 根据用户id查询猪场信息列表
     * @param userId 用户id
     * @return 猪场信息列表
     */
    Response<List<DoctorFarm>> findFarmsByUserId(Long userId);

    /**
     * 查询所有猪场
     * @return 所有猪场
     */
    Response<List<DoctorFarm>> findAllFarms();
}
