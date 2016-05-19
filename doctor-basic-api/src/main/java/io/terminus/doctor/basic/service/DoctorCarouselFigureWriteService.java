package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;

/**
 * Desc: 轮播图写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */

public interface DoctorCarouselFigureWriteService {

    /**
     * 创建轮播图
     * @param carouselFigure 轮播图
     * @return 是否成功
     */
    Response<Long> createFigure(DoctorCarouselFigure carouselFigure);

    /**
     * 更需吧轮播图
     * @param carouselFigure 轮播图
     * @return 是否成功
     */
    Response<Boolean> updateFigure(DoctorCarouselFigure carouselFigure);

    /**
     * 删除轮播图
     * @param figureId 轮播图id
     * @return 是否成功
     */
    Response<Boolean> deleteFigure(Long figureId);
}
