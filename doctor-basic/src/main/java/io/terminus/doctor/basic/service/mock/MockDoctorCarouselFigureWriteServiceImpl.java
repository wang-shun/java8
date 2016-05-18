package io.terminus.doctor.basic.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import io.terminus.doctor.basic.service.DoctorCarouselFigureWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */
@Slf4j
@Service
@Primary
public class MockDoctorCarouselFigureWriteServiceImpl implements DoctorCarouselFigureWriteService {

    @Override
    public Response<Boolean> createFigure(DoctorCarouselFigure carouselFigure) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> updateFigure(DoctorCarouselFigure carouselFigure) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> deleteFigure(Long id) {
        return Response.ok(Boolean.TRUE);
    }
}
