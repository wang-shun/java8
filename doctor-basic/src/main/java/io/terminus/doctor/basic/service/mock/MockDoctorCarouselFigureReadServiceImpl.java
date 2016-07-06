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
        f1.setUrl("http://img.xrnm.com/20160616-ad115c7ce911aaa925327ecee812fbfc.png");
    //    f1.setForward("https://www.baidu.com/");

        DoctorCarouselFigure f2 = new DoctorCarouselFigure();
        f2.setId(2L);
        f2.setIndex(2);
        f2.setStatus(1);
        f2.setUrl("http://img.xrnm.com/20160616-85900d92c238fe79afa3e1b45a865f9f.png");
    //    f2.setForward("https://www.baidu.com/");

        DoctorCarouselFigure f3 = new DoctorCarouselFigure();
        f3.setId(3L);
        f3.setIndex(3);
        f3.setStatus(1);
        f3.setUrl("http://img.xrnm.com/20160616-f9c07f1b71f08a3e85ccea3188f15a54.png");
    //    f3.setForward("https://www.baidu.com/");
        return Response.ok(Lists.newArrayList(f1, f2, f3));
    }

    @Override
    public Response<DoctorCarouselFigure> findFigureById(Long id) {
        DoctorCarouselFigure f1 = new DoctorCarouselFigure();
        f1.setId(1L);
        f1.setIndex(1);
        f1.setStatus(1);
        f1.setUrl("http://img.xrnm.com/20160616-ad115c7ce911aaa925327ecee812fbfc.png");
    //    f1.setForward("https://www.baidu.com/");
        return Response.ok(f1);
    }
}
