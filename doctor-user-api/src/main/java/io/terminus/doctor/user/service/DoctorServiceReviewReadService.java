package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.model.DoctorServiceReview;

import java.util.List;

/**
 * Desc: 用户服务审批读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorServiceReviewReadService {

    /**
     * 根据id查询用户审批服务
     * @param reviewId 服务审批表id
     * @return 用户审批服务
     */
    Response<DoctorServiceReview> findServiceReviewById(Long reviewId);

    /**
     * 根据用户id查询审批list
     * @param userId 用户id
     * @return 审批服务list
     */
    Response<List<DoctorServiceReview>> findServiceReviewsByUserId(Long userId);

    /**
     * 根据用户id和服务类型查询审批服务
     * @param userId 用户id
     * @param type 服务类型
     * @return 用户审批服务
     */
    Response<DoctorServiceReview> findServiceReviewByUserIdAndType(Long userId, Integer type);

    /**
     * 根据用户id和服务类型查询审批服务dto(列转行)
     * @param userId 用户id
     * @return 用户审批服务dto
     */
    Response<DoctorServiceReviewDto> findServiceReviewDtoByUserId(Long userId);
}
