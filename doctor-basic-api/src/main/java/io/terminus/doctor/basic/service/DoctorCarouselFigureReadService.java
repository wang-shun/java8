package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;

import java.util.List;

/**
 * Desc: 轮播图读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/17
 */

public interface DoctorCarouselFigureReadService {

    /**
     * 根据状态查询轮播图
     * @param status 状态: 1 启用, -1 不启用
     * @return 轮播图list
     */
    Response<List<DoctorCarouselFigure>> findFiguresByStatus(Integer status);

    /**
     * 查询所有轮播图
     * @return 轮播图list
     */
    Response<List<DoctorCarouselFigure>> findAllFigures();

    /**
     * 查询启用的轮播图
     * @return 轮播图list
     */
    Response<List<DoctorCarouselFigure>> findActingFigures();
}
