package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
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
    Response<DoctorServiceReview> findServiceReviewByUserIdAndType(Long userId, DoctorServiceReview.Type type);

    /**
     * 分页查询, 所有参数都可以为空
     * @param userId 申请用户的id
     * @param userMobile 用户注册时的手机号
     * @param type 参见枚举 DoctorServiceReview.Type
     * @param status 枚举 DoctorServiceReview.Status
     * @return
     */
    Response<Paging<DoctorServiceReview>> page(Integer pageNo, Integer pageSize, Long userId, String userMobile,
                                               DoctorServiceReview.Type type, DoctorServiceReview.Status status);
}
