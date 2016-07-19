package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪场日报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Repository
public class DoctorDailyReportDao extends MyBatisDao<DoctorDailyReport> {

    public List<DoctorDailyReport> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
}
