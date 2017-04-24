package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorGroupChangeSum;
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

    /**
     * 获取某一段时间的猪群存栏变化
     * @param farmId
     * @param sumAt
     * @return
     */
    public DoctorGroupChangeSum getGroupChangeSum(Long farmId, String sumAt) {
        return getGroupChangeSum(farmId, sumAt, sumAt);
    }

    /**
     * 获取某一段时间的猪群存栏变化
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public DoctorGroupChangeSum getGroupChangeSum(Long farmId, Date startAt, Date endAt) {
        return getGroupChangeSum(farmId, DateUtil.toDateString(startAt), DateUtil.toDateString(endAt));
    }

    /**
     * 获取某一段时间的猪群存栏变化
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public DoctorGroupChangeSum getGroupChangeSum(Long farmId, String startAt, String endAt) {
        return getSqlSession().selectOne(sqlId("getGroupChangeSum"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 获取猪群每日报表
     * @param groupId
     * @param sumAt
     * @return
     */
    public DoctorDailyGroup findByGroupIdAndSumAt(Long groupId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("findByGroupIdAndSumAt"), ImmutableMap.of("groupId", groupId, "sumAt", sumAt));
    }
    /**
     * 更新日期之后每日猪群存栏
     * @param groupId 猪群id
     * @param sumAt 日期
     * @param changeCount 变动数量
     */
    public void updateDailyGroupLiveStock(Long groupId, Date sumAt, Integer changeCount) {
        getSqlSession().update(sqlId("updateDailyGroupLiveStock"), ImmutableMap.of("groupId", groupId, "sumAt", sumAt, "changeCount", changeCount));
    }

    public Integer findFattenWillOut(Long farmId, String sumAt) {
        return getSqlSession().selectOne(sqlId("findFattenWillOut"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    /**
     * 查询某一天所有猪群的存栏信息
     * @param sumAt
     * @return
     */
    public List<DoctorDailyGroup> findGroupInfoBySumAt(String sumAt) {
        return getSqlSession().selectList(sqlId("findGroupInfoBySumAt"), ImmutableMap.of("sumAt", sumAt));
    }

    /**
     * 查询某一猪群某天后包括某天的记录
     * @param groupId 猪群id
     * @param sumAt 统计开始时间(包括)
     * @return
     */
    public List<DoctorDailyGroup> findAfterSumAt(Long groupId, String sumAt) {
        return getSqlSession().selectList(sqlId("findAfterSumAt"), ImmutableMap.of("groupId", groupId, "sumAt", sumAt));
    }
}
