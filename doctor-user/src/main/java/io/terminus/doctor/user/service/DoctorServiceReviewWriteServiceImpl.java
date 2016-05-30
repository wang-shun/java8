package io.terminus.doctor.user.service;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.manager.DoctorUserManager;
import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Primary
public class DoctorServiceReviewWriteServiceImpl implements DoctorServiceReviewWriteService{
    private final DoctorServiceReviewDao doctorServiceReviewDao;
    private final DoctorUserManager doctorUserManager;

    @Autowired
    public DoctorServiceReviewWriteServiceImpl(DoctorServiceReviewDao doctorServiceReviewDao, DoctorUserManager doctorUserManager){
        this.doctorServiceReviewDao = doctorServiceReviewDao;
        this.doctorUserManager = doctorUserManager;
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
    public Response<Boolean> applyOpenService(BaseUser baseUser, DoctorServiceApplyDto serviceApplyDto) {
        Response<Boolean> response = new Response<>();
        try {
            doctorUserManager.applyOpenService(baseUser, serviceApplyDto);
            response.setResult(true);
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("applyOpenService failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("apply.open.service.failed");
        }
        return response;
    }
}
