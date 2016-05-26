package io.terminus.doctor.basic.service.mock;

import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import io.terminus.doctor.basic.service.DoctorCarouselFigureReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */
@Slf4j
@Service
@Primary
public class MockDoctorCarouselFigureReadServiceImpl implements DoctorCarouselFigureReadService {

    @Override
    public Response<List<DoctorCarouselFigure>> findFiguresByStatus(Integer status) {
        DoctorCarouselFigure f1 = new DoctorCarouselFigure();
        f1.setId(1L);
        f1.setIndex(1);
        f1.setStatus(1);
        f1.setUrl("http://img.xrnm.com/20150816-1b959d0ae008e92d72da4ba46ab0f04b.jpg");
        f1.setForward("https://www.baidu.com/");

        DoctorCarouselFigure f2 = new DoctorCarouselFigure();
        f2.setId(2L);
        f2.setIndex(2);
        f2.setStatus(1);
        f2.setUrl("http://img.xrnm.com/20150816-2995c70688bef24516f9137eb9e31ae4.jpg");
        f2.setForward("https://www.baidu.com/");

        DoctorCarouselFigure f3 = new DoctorCarouselFigure();
        f3.setId(3L);
        f3.setIndex(3);
        f3.setStatus(1);
        f3.setUrl("http://img.xrnm.com/20150816-92bbf23989e2e66e7db8fc856aadbe6d.jpg");
        f3.setForward("https://www.baidu.com/");
        return Response.ok(Lists.newArrayList(f1, f2, f3));
    }

    @Override
    public Response<DoctorCarouselFigure> findFigureById(Long id) {
        DoctorCarouselFigure f1 = new DoctorCarouselFigure();
        f1.setId(1L);
        f1.setIndex(1);
        f1.setStatus(1);
        f1.setUrl("http://img.xrnm.com/20150816-1b959d0ae008e92d72da4ba46ab0f04b.jpg");
        f1.setForward("https://www.baidu.com/");
        return Response.ok(f1);
    }
}
