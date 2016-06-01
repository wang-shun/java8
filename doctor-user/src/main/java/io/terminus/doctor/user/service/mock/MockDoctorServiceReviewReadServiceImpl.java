package io.terminus.doctor.user.service.mock;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.user.dto.DoctorServiceReviewDto;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class MockDoctorServiceReviewReadServiceImpl implements DoctorServiceReviewReadService {
    @Override
    public Response<DoctorServiceReview> findServiceReviewById(Long reviewId) {
        return null;
    }

    @Override
    public Response<List<DoctorServiceReview>> findServiceReviewsByUserId(Long userId) {
        return null;
    }

    @Override
    public Response<DoctorServiceReview> findServiceReviewByUserIdAndType(Long userId, DoctorServiceReview.Type type) {
        return Response.ok(mockDoctorServiceReview(userId, type.getValue()));
    }

    @Override
    public Response<DoctorServiceReviewDto> findServiceReviewDtoByUserId(Long userId) {
        return Response.ok(mockDoctorServiceReviewDto(userId));
    }

    private DoctorServiceReviewDto mockDoctorServiceReviewDto(Long userId) {
        DoctorServiceReviewDto reviewDto = new DoctorServiceReviewDto();
        reviewDto.setUserId(userId);
        reviewDto.setPigDoctor(mockDoctorServiceReview(userId, 1));
        reviewDto.setPigmall(mockDoctorServiceReview(userId, 2));
        reviewDto.setNeverest(mockDoctorServiceReview(userId, 3));
        reviewDto.setPigTrade(mockDoctorServiceReview(userId, 4));
        return reviewDto;
    }

    private DoctorServiceReview mockDoctorServiceReview(Long userId, Integer type) {
        DoctorServiceReview review = new DoctorServiceReview();
        review.setId(userId);
        review.setUserId(userId);
        review.setType(type);
        review.setStatus(RandomUtil.random(-2, 2));
        return review;
    }

    @Override
    public Response<Paging<DoctorServiceReview>> page(Integer pageNo, Integer pageSize, Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status status){
        return Response.ok(new Paging<>(1L, Lists.newArrayList(new DoctorServiceReview())));
    }
}
