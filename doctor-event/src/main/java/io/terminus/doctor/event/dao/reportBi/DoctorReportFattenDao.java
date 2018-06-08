package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportFatten;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:15:16
 * Created by [ your name ]
 */
@Repository
public class DoctorReportFattenDao extends MyBatisDao<DoctorReportFatten> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportFatten findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }

    public List<DoctorReportFatten> findBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("findBy"), dimensionCriteria);
    }

    public DoctorReportFatten sumBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("sumBy"), dimensionCriteria);
    }
}
