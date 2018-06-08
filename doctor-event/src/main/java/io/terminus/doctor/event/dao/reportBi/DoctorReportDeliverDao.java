package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 14:33:07
 * Created by [ your name ]
 */
@Repository
public class DoctorReportDeliverDao extends MyBatisDao<DoctorReportDeliver> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportDeliver findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }

    public List<DoctorReportDeliver> findBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("findBy"), dimensionCriteria);
    }

    public DoctorReportDeliver sumBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("sumBy"), dimensionCriteria);
    }
}
