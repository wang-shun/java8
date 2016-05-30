package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class MockDoctorServiceReviewWriteServiceImpl implements DoctorServiceReviewWriteService {
    @Override
    public Response<Long> createReview(DoctorServiceReview review) {
        return Response.ok(1L);
    }

    @Override
    public Response<Boolean> updateReview(DoctorServiceReview review) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> deleteReview(Long reviewId) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> updateStatus(BaseUser user, Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status newStatus) {
        return Response.ok(Boolean.TRUE);
    }

}
