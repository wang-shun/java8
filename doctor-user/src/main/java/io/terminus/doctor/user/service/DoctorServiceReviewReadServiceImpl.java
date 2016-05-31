package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@Primary
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
    public Response<DoctorServiceReview> findServiceReviewByUserIdAndType(Long userId, Integer type) {
        Response<DoctorServiceReview> response = new Response<>();
        try {
            response.setResult(doctorServiceReviewDao.findByUserIdAndType(userId, DoctorServiceReview.Type.from(type)));
        } catch (Exception e) {
            log.error("find doctor service review failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.doctor.service.review.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorServiceReviewDto> findServiceReviewDtoByUserId(Long userId) {
        Response<DoctorServiceReviewDto> response = new Response<>();
        DoctorServiceReviewDto dto = new DoctorServiceReviewDto();
        dto.setUserId(userId);
        try {
            for (DoctorServiceReview review : doctorServiceReviewDao.findByUserId(userId)){
                if (DoctorServiceReview.Type.PIG_DOCTOR.getValue() == review.getType()) {
                    dto.setPigDoctor(review);
                } else if (DoctorServiceReview.Type.PIGMALL.getValue() == review.getType()) {
                    dto.setPigmall(review);
                } else if (DoctorServiceReview.Type.NEVEREST.getValue() == review.getType()) {
                    dto.setNeverest(review);
                } else if (DoctorServiceReview.Type.PIG_TRADE.getValue() == review.getType()) {
                    dto.setPigTrade(review);
                } else {
                    log.error("doctor service review type error, type = {}", review.getType());
                    return Response.fail("doctor.service.review.type.error");
                }
            }
            response.setResult(dto);
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
