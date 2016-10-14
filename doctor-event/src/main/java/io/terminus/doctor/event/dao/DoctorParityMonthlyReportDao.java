package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.model.DoctorParityMonthlyReport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 胎次产仔分析月报Dao类
 * Date: 2016-09-13
 */
@Repository
public class DoctorParityMonthlyReportDao extends MyBatisDao<DoctorParityMonthlyReport> {

    public void deleteByFarmIdAndSumAt(Long farmId, Date sumAt) {
        getSqlSession().delete(sqlId("deleteByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    /**
     * 生产产仔胎次分析月报
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public List<DoctorParityMonthlyReport> constructDoctorParityMonthlyReports(Long farmId, Date startAt, Date endAt){
        return getSqlSession().selectList(sqlId("constructDoctorParityMonthlyReports"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }


    /**
     * 生产产仔胎次分析月报
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public List<DoctorParityMonthlyReport> constructDoctorParityMonthlyReports(Long farmId, Integer parityStart, Integer parityEnd, Date startAt, Date endAt){
        return getSqlSession().selectList(sqlId("constructDoctorParityMonthlyReportsWithParity"), MapBuilder.<String, Object>of().put("farmId", farmId).put("parityStart", parityStart)
                .put("parityEnd", parityEnd).put("startAt", startAt).put("endAt", endAt).map());
    }

    public List<DoctorParityMonthlyReport> findDoctorParityMonthlyReports(Long farmId, String sumAt){
        return getSqlSession().selectList(sqlId("findDoctorParityMonthlyReports"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

}
