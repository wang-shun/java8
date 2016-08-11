package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪场月报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Repository
public class DoctorMonthlyReportDao extends MyBatisDao<DoctorMonthlyReport> {

    public List<DoctorMonthlyReport> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
}
