package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class DoctorServiceReviewWriteServiceImpl implements DoctorServiceReviewWriteService{
    private final DoctorServiceReviewDao doctorServiceReviewDao;

    @Autowired
    public DoctorServiceReviewWriteServiceImpl(DoctorServiceReviewDao doctorServiceReviewDao){
        this.doctorServiceReviewDao = doctorServiceReviewDao;
    }

    @Override
    public Response<Long> createReview(DoctorServiceReview review) {
        Response<Long> response = new Response<>();
        try {
            doctorServiceReviewDao.create(review);
            response.setResult(review.getId());
        } catch (Exception e) {
            log.error("create doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("create.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateReview(DoctorServiceReview review) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.update(review));
        } catch (Exception e) {
            log.error("update doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteReview(Long reviewId) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.delete(reviewId));
        } catch (Exception e) {
            log.error("delete doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> initServiceReview(Long userId, String userMobile){
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.initData(userId, userMobile));
        } catch (Exception e) {
            log.error("init doctor service review failed, userId={}, cause : {}", userId, Throwables.getStackTraceAsString(e));
            response.setError("init.doctor.service.review.failed");
        }
        return response;
    }
}
