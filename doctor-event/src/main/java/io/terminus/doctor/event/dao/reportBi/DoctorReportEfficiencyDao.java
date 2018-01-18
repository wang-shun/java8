package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:13:08
 * Created by [ your name ]
 */
@Repository
public class DoctorReportEfficiencyDao extends MyBatisDao<DoctorReportEfficiency> {
    public DoctorReportEfficiency findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }
}
