package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportBoar;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 14:28:37
 * Created by [ your name ]
 */
@Repository
public class DoctorReportBoarDao extends MyBatisDao<DoctorReportBoar> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
    public DoctorReportBoar findByDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
    }
}
