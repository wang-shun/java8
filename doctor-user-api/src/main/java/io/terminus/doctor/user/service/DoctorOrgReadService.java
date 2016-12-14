package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrg;

import java.util.List;

public interface DoctorOrgReadService {

    /**
     * 根据公司id查询公司信息
     * @param orgId 公司id
     * @return 公司信息
     */
    Response<DoctorOrg> findOrgById(Long orgId);

    /**
     * 根据公司id查询公司信息
     * @param orgIds 公司ids
     * @return 公司信息
     */
    Response<List<DoctorOrg>> findOrgByIds(List<Long> orgIds);

    /**
     * 根据用户id查询公司信息
     * @param userId 用户id
     * @return 公司信息
     */
    Response<DoctorOrg> findOrgByUserId(Long userId);
}
