package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorServiceReviewExt;
import io.terminus.doctor.user.model.DoctorServiceStatus;

/**
 * @author 陈增辉
 * Desc: 用户服务状态表读服务
 * Date: 2016-06-03
 */

public interface DoctorServiceStatusReadService {

    /**
     * 根据id查询用户服务状态表
     * @param id 主键id
     * @return 用户服务状态表
     */
    Response<DoctorServiceStatus> findById(Long id);

    /**
     * 根据userId查询用户的服务开通情况
     * @param userId
     * @return
     */
    Response<DoctorServiceStatus> findByUserId(Long userId);

    Response<Paging<DoctorServiceReviewExt>> page(Long userId, Integer type, String userMobile, String orgName, Integer pageNo, Integer pageSize);

}
