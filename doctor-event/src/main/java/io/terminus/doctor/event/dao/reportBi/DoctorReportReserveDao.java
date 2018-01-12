package io.terminus.doctor.event.dao.reportBi;

import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 14:25:01
 * Created by [ your name ]
 */
@Repository
public class DoctorReportReserveDao extends MyBatisDao<DoctorReportReserve> {
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
}
