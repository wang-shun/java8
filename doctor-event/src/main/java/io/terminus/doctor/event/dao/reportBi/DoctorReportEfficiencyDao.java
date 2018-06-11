package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:13:08
 * Created by [ your name ]
 */
@Repository
public class DoctorReportEfficiencyDao extends MyBatisDao<DoctorReportEfficiency> {
    public DoctorReportEfficiency findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        DoctorReportEfficiency efficiency = getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);


        if (null == efficiency) {
            efficiency = new DoctorReportEfficiency();
            efficiency.setOrzId(dimensionCriteria.getOrzId());
            efficiency.setSumAt(dimensionCriteria.getSumAt());
            efficiency.setLactation(0);
            efficiency.setPregnancy(0);
        }

        return efficiency;
    }

    public List<DoctorReportEfficiency> findBy(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("findBy"), dimensionCriteria);
    }

    public void delete() {
        this.sqlSession.delete(sqlId("deleteAll"));
    }


    public void delete(DoctorDimensionCriteria criteria) {
        this.sqlSession.delete(sqlId("deleteBy"), criteria);
    }
}
