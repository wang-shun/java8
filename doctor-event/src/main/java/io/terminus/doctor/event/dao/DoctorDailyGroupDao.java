package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupStock;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数量每天记录表Dao类
 * Date: 2017-04-17
 */
@Repository
public class DoctorDailyGroupDao extends MyBatisDao<DoctorDailyGroup> {

    /**
     * 删除某一天的猪群统计
     * @param farmId
     * @param date
     */
    public void deleteByFarmIdAndSumAt(Long farmId, Date date) {
        getSqlSession().delete(sqlId("deleteByFarmIdAndSumAt"), ImmutableMap.of("farmId", farmId, "sumAt", date));
    }

    /**
     * 删除某一天的猪群统计
     * @param date
     */
    public void deleteByFarmIdAndSumAt(Date date) {
        getSqlSession().delete(sqlId("deleteBySumAt"), ImmutableMap.of("sumAt", date));
    }

    public List<DoctorDailyGroup> getDoctorGroupSum(Long farmId, Date date) {
        return getSqlSession().selectList(sqlId("getDoctorGroupSumBySumAt"), ImmutableMap.of("farmId", farmId, "sumAt", date));
    }

    public List<DoctorDailyGroup> getDoctorGroupSumByRange(Long farmId, Date startAt, Date endAt) {
        return getSqlSession().selectList(sqlId("getDoctorGroupSumByRange"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 获取某一天的存栏
     * @param farmId
     * @param sumAt
     * @return
     */
    public DoctorGroupStock getGroupStock(Long farmId, String sumAt) {
        return getSqlSession().selectOne(sqlId("getGroupStock"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    /**
     * 获取某一天的存栏
     * @param farmId
     * @param sumAt
     * @return
     */
    public DoctorGroupStock getGroupStock(Long farmId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("getGroupStock"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public List<DoctorDailyReport> findBySumAt(Date sumAt) {
        return getSqlSession().selectList(sqlId("findBySumAt"), ImmutableMap.of("sumAt", sumAt));
    }
}
