package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportSow;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:25:01
 * Created by [ your name ]
 */
@Repository
public class DoctorReportSowDao extends MyBatisDao<DoctorReportSow> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportSow findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }
}
