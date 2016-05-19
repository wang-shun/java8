package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;

import java.util.List;

/**
 * Desc: 猪场信息读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmReadService {

    /**
     * 根据公司id查询公司信息
     * @param orgId 公司id
     * @return 公司信息
     */
    Response<DoctorOrg> findOrgById(Long orgId);

    /**
     * 根据用户id查询公司信息
     * @param userId 用户id
     * @return 公司信息
     */
    Response<DoctorOrg> findOrgByUserId(Long userId);

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
}
