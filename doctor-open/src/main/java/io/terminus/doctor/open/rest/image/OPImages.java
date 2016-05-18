package io.terminus.doctor.open.rest.image;

import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import io.terminus.doctor.basic.service.DoctorCarouselFigureReadService;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Desc: 图片相关
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@OpenBean
public class OPImages {

    private final DoctorCarouselFigureReadService doctorCarouselFigureReadService;

    @Autowired
    public OPImages(DoctorCarouselFigureReadService doctorCarouselFigureReadService) {
        this.doctorCarouselFigureReadService = doctorCarouselFigureReadService;
    }

    /**
     * 查询轮播图
     * @return 轮播图list
     */
    @OpenMethod(key = "get.carousel.figure")
    public List<DoctorCarouselFigure> getCarouselFigures() {
        return OPRespHelper.orOPEx(doctorCarouselFigureReadService.findFiguresByStatus(1));
    }
}
