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

    Response<Long> createReview(DoctorServiceReview review);

    Response<Boolean> updateReview(DoctorServiceReview review);

    Response<Boolean> deleteReview(Long reviewId);

}
