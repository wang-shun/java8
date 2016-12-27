package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Desc: 猪场日报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Repository
public class DoctorDailyReportDao extends MyBatisDao<DoctorDailyReport> {

    public void updateByFarmIdAndSumAt(DoctorDailyReport report) {
        getSqlSession().update(sqlId("updateByFarmIdAndSumAt"), report);
    }

    public DoctorDailyReport findByFarmIdAndSumAt(Long farmId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public List<DoctorDailyReport> findBySumAt(Date sumAt) {
        return getSqlSession().selectList(sqlId("findBySumAt"), ImmutableMap.of("sumAt", sumAt));
    }

    public void deleteBySumAt(Date sumAt) {
        getSqlSession().delete(sqlId("deleteBySumAt"), sumAt);
    }

    public void deleteByFarmIdAndSumAt(Long farmId, Date sumAt) {
        getSqlSession().delete(sqlId("deleteByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }
}
