package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Repository
public class DoctorPigDailyDao extends MyBatisDao<DoctorPigDaily> {

    public DoctorPigDaily findBy(Long farmId, String sumAt) {
        return sqlSession.selectOne(sqlId("findBy"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public DoctorPigDaily findBy(Long farmId, Date sumAt) {
        return findBy(farmId, DateUtil.toDateString(sumAt));
    }

    public DoctorPigDaily countByFarm(Long farmId, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("startDate", start);
        params.put("endDate", end);

        return sqlSession.selectOne(this.sqlId("report"), params);
    }


    /**
     * 求指定期限内母猪的存栏之和
     * 产房母猪+配坏母猪
     *
     * @return
     */
    public Integer countSow(Long farmId, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("startDate", start);
        params.put("endDate", end);
        Map<String, BigDecimal> sowCount = sqlSession.selectOne(this.sqlId("countSow"), params);
        return sowCount.get("sow_count").intValue();
    }


    public DoctorPigDaily countByOrg(List<Long> farmIds, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("startDate", start);
        params.put("endDate", end);

        return sqlSession.selectOne(this.sqlId("report"), params);
    }

    public DoctorPigDaily countByOrg(Long orgId, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        params.put("startDate", start);
        params.put("endDate", end);

        return sqlSession.selectOne(this.sqlId("reportByOrg"), params);
    }

    /**
     * 更新日期(包括更新日期)之后每日母猪存栏
     *
     * @param farmId          猪群id
     * @param sumAt           日期
     * @param liveChangeCount 存栏变动数量
     * @param phChangeCount   配怀舍存栏变化
     * @param cfChangeCount   产房存栏变化
     */
    public void updateDailySowPigLiveStock(Long farmId, Date sumAt, Integer liveChangeCount, Integer phChangeCount, Integer cfChangeCount) {
        getSqlSession().update(sqlId("updateDailySowPigLiveStock"), MapBuilder.of().put("farmId", farmId)
                .put("sumAt", DateUtil.toDateString(sumAt)).put("liveChangeCount", liveChangeCount)
                .put("phChangeCount", phChangeCount).put("cfChangeCount", cfChangeCount).map());
    }

    /**
     * 更新日期(包括更新日期)之后每日公猪存栏
     *
     * @param farmId      猪群id
     * @param sumAt       日期
     * @param changeCount 变动数量
     */
    public void updateDailyBoarPigLiveStock(Long farmId, Date sumAt, Integer changeCount) {
        getSqlSession().update(sqlId("updateDailyBoarPigLiveStock"), ImmutableMap.of("farmId"
                , farmId, "sumAt", DateUtil.toDateString(sumAt), "changeCount", changeCount));
    }

    /**
     * 更新日期(包括更新日期)之后配怀舍个状态母猪数
     * @param farmId 猪场id
     * @param sumAt 日期
     * @param phMatingChangeCount 配种母猪变化量
     * @param phKonghuaiChangeCount 空怀母猪变化量
     * @param phPregnantChangeCount 怀孕母猪变化量
     */
    public void updateDailyPhStatusLiveStock(Long farmId, Date sumAt, Integer phMatingChangeCount
            , Integer phKonghuaiChangeCount, Integer phPregnantChangeCount) {
        getSqlSession().update(sqlId("updateDailyPhStatusLiveStock"), ImmutableMap.of("farmId"
                , farmId, "sumAt", DateUtil.toDateString(sumAt), "phMatingChangeCount", phMatingChangeCount
                , "phKonghuaiChangeCount", phKonghuaiChangeCount, "phPregnantChangeCount", phPregnantChangeCount));
    }

    /**
     * 查询指定之间之后的日报，包含指定日期
     *
     * @param farmId 猪场id
     * @param sumAt  指定日期
     * @return
     */
    public List<DoctorPigDaily> queryAfterSumAt(Long farmId, Date sumAt) {
        return getSqlSession().selectList(sqlId("queryAfterSumAt"),
                ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    /**
     * 查询指定组织维度和时间维度下的聚合数据（不包含猪场日维度）
     *
     * @param dimensionCriteria 维度
     * @return 聚合数据
     */
    public List<DoctorPigDailyExtend> sumForDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("sumForDimension"), dimensionCriteria);
    }

    /**
     * 获取某一维度的数据
     *
     * @param dimensionCriteria
     * @return
     */
    public DoctorPigDailyExtend selectOneSumForDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("selectOneSumForDimension"), dimensionCriteria);
    }

    public List<DoctorPigDaily> findByAfter(Long orzId, Integer orzType, Date sumAt, Integer type){
        return getSqlSession().selectList(sqlId("findByAfter"), MapBuilder.of().put("orzId", orzId)
                .put("orzType", orzType).put("sumAt", sumAt).put("type", type).map());
    }

    public List<DoctorDimensionCriteria> findByDateType(Long orzId, Date sumAt, Integer type,  Integer dateType, Integer orzType) {
        return getSqlSession().selectList(sqlId("findByDateType"), MapBuilder.of().put("orzId", orzId).put("type", type)
                .put("sumAt", sumAt).put("dateType", dateType).put("orzType", orzType).map());
    }

    public DoctorPigDailyExtend farmStart(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("farmStart"), dimensionCriteria);
    }

    public DoctorPigDailyExtend farmEnd(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("farmEnd"), dimensionCriteria);
    }

    public DoctorPigDailyExtend orgStart(Long orgId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("orgStart"), ImmutableMap.of("orgId", orgId, "sumAt", sumAt));
    }

    public DoctorPigDailyExtend orgEnd(Long orgId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("orgEnd"), ImmutableMap.of("orgId", orgId, "sumAt", sumAt));
    }

    public DoctorPigDailyExtend orgSumDimension(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("orgSumDimension"), dimensionCriteria);
    }

    public Integer countDimension(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("countDimension"), dimensionCriteria);
    }

    public DoctorPigDailyExtend orgDayStock(Long orgId, Date sumAt){
        return getSqlSession().selectOne(sqlId("orgDayStock"), ImmutableMap.of("orgId", orgId, "sumAt", sumAt));
    }

    public Date minDate(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("minDate"), dimensionCriteria);
    }

    public Date maxDate(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("maxDate"), dimensionCriteria);
    }

    public Date minSumAtForUpdated(Long orzId, Integer orzType, Date updateAt){
        return getSqlSession().selectOne(sqlId("minSumAtForUpdated"),
                ImmutableMap.of("orzId", orzId, "orzType", orzType, "updateAt", updateAt));
    }

    public List<Map<String, Object>> orgMinDate(){
        return getSqlSession().selectList(sqlId("orgMinDate"));
    }


    //孔景军
    public DoctorPigDailyExtend groupStart(Long groupId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("groupStart"), ImmutableMap.of("groupId", groupId, "sumAt", sumAt));
    }

    public DoctorPigDailyExtend groupEnd(Long groupId, Date sumAt) {
        return getSqlSession().selectOne(sqlId("groupEnd"), ImmutableMap.of("groupId", groupId, "sumAt", sumAt));
    }

    public List<Long> getAllGroup(){
        return getSqlSession().selectList(sqlId("getAllGroup"));
    }

    //通过公司id查集团id(孔景军)
    public Long getGroupIdByOrgId(Long orgId){
        return getSqlSession().selectOne(sqlId("getGroupIdByOrgId"),orgId);
    }
}
