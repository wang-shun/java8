package io.terminus.doctor.user.service;

import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorServiceReview;

/**
 * Desc: 用户服务审批写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorServiceReviewWriteService {

    /**
     * 创建一条服务申请审批数据
     * @param review
     * @return 主键
     */
    Response<Long> createReview(DoctorServiceReview review);

    /**
     * 更新一条服务申请审批数据
     * @param review
     * @return 是否成功
     */
    Response<Boolean> updateReview(DoctorServiceReview review);


    /**
     * 删除一条服务申请审批数据
     * @param reviewId 主键
     * @return
     */
    Response<Boolean> deleteReview(Long reviewId);

    /**
     * 用户服务申请审批数据初始化
     * @param userId 用户id
     * @return 是否成功
     */
    Response<Boolean> initServiceReview(Long userId, String userMobile);
}
