package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class DoctorServiceReviewReadServiceImpl implements DoctorServiceReviewReadService{
    private final DoctorServiceReviewDao doctorServiceReviewDao;

    @Autowired
    public DoctorServiceReviewReadServiceImpl(DoctorServiceReviewDao doctorServiceReviewDao){
        this.doctorServiceReviewDao = doctorServiceReviewDao;
    }

    @Override
    public Response<DoctorServiceReview> findServiceReviewById(Long reviewId) {
        Response<DoctorServiceReview> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.findById(reviewId));
        } catch (Exception e) {
            log.error("find doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorServiceReview>> findServiceReviewsByUserId(Long userId) {
        Response<List<DoctorServiceReview>> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.findByUserId(userId));
        } catch (Exception e) {
            log.error("find doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorServiceReview> findServiceReviewByUserIdAndType(Long userId, DoctorServiceReview.Type type) {
        Response<DoctorServiceReview> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.findByUserIdAndType(userId, type));
        } catch (Exception e) {
            log.error("find doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<Paging<DoctorServiceReview>> page(Integer pageNo, Integer pageSize, Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status status){
        Response<Paging<DoctorServiceReview>> response = new Response<>();
        Map<String, Object> criteria = Maps.newHashMap();
        if (type != null) {
            criteria.put("type", type.getValue());
        }
        if (status != null) {
            criteria.put("status", status.getValue());
        }
        criteria.put("userId", userId);
        PageInfo pageInfo = new PageInfo(pageNo, pageSize);
        criteria.putAll(pageInfo.toMap());
        try{
            response.setResult(doctorServiceReviewDao.paging(criteria));
        }catch(Exception e){
            log.error("page DoctorServiceReview failed,  cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("page.doctor.service.review.failed");
        }
        return response;
    }
}
