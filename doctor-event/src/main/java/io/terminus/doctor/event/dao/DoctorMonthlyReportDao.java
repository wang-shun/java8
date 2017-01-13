package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Desc: 猪场月报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Repository
public class DoctorMonthlyReportDao extends MyBatisDao<DoctorMonthlyReport> {

    /**
     * 根据猪场id和统计日期查询
     * @param farmId 猪场id
     * @param sumAt  统计日期
     * @return 猪场月报
     */
    public DoctorMonthlyReport findByFarmIdAndSumAt(Long farmId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", Dates.startOfDay(sumAt)));
    }

    /**
     * 根据统计日期删除月报统计
     * @param sumAt 统计日期
     */
    public void deleteBySumAt(Date sumAt) {
        getSqlSession().delete(sqlId("deleteBySumAt"), Dates.startOfDay(sumAt));
    }

    public void deleteByFarmIdAndSumAt(Long farmId, Date sumAt) {
        getSqlSession().delete(sqlId("deleteByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", Dates.startOfDay(sumAt)));
    }
}
