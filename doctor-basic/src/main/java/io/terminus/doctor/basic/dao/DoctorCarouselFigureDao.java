package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorCarouselFigure;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 轮播图表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorCarouselFigureDao extends MyBatisDao<DoctorCarouselFigure> {

    /**
     * 根据状态查询
     * @param status 状态
     * @return 轮播图list
     */
    public List<DoctorCarouselFigure> findByStatus(Integer status) {
        return getSqlSession().selectList(sqlId("findByStatus"), status);
    }
}
