package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Desc: 猪场日报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Repository
public class DoctorDailyReportDao extends MyBatisDao<DoctorDailyReport> {

    public DoctorDailyReport findByFarmIdAndSumAt(Long farmId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public void deleteBySumAt(Date sumAt) {
        getSqlSession().delete(sqlId("deleteBySumAt"), sumAt);
    }
}
