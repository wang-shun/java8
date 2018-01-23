package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorGroupDailyDao extends MyBatisDao<DoctorGroupDaily> {

    public DoctorGroupDaily findBy(Long farmId, Integer pigType, String sumAt) {
        return sqlSession.selectOne(sqlId("findBy"), ImmutableMap.of("farmId", farmId,
                "pigType", pigType, "sumAt", sumAt));
    }

    public DoctorGroupDaily findBy(Long farmId, Integer pigType, Date sumAt) {
        return findBy(farmId, pigType, DateUtil.toDateString(sumAt));
    }

    /**
     * 更新日期之后每日猪群存栏
     *
     * @param farmId    猪场id
     * @param pigType   猪群类型
     * @param sumAt       日期
     * @param changeCount 变动数量
     */
    public void updateDailyGroupLiveStock(Long farmId, Integer pigType, Date sumAt, Integer changeCount) {
        getSqlSession().update(sqlId("updateDailyGroupLiveStock"), ImmutableMap.of("farmId", farmId, "pigType", pigType,
                "sumAt", DateUtil.toDateString(sumAt), "changeCount", changeCount));
    }

    /**
     * 查询指定之间之后的日报，包含指定日期
     * @param farmId 猪场id
     * @param sumAt 指定日期
     * @return
     */
    public List<DoctorGroupDaily> queryAfterSumAt(Long farmId, Integer pigType, Date sumAt) {
        return getSqlSession().selectList(sqlId("queryAfterSumAt"),
                ImmutableMap.of("farmId", farmId, "pigType", pigType, "sumAt", sumAt));
    }

    /**
     * 查询指定组织维度和时间维度下的聚合数据（不包含猪场日维度）
     * @param dimensionCriteria 维度
     * @return 聚合数据
     */
    public List<DoctorGroupDailyExtend> sumForDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectList(sqlId("sumForDimension"), dimensionCriteria);
    }

    /**
     * 获取某一维度的数据
     * @param dimensionCriteria
     * @return
     */
    public DoctorGroupDailyExtend selectOneSumForDimension(DoctorDimensionCriteria dimensionCriteria) {
        return getSqlSession().selectOne(sqlId("selectOneSumForDimension"), dimensionCriteria);
    }

    public List<DoctorGroupDaily> findByAfter(Long farmId, Date sumAt, Date updatedAt){
        return getSqlSession().selectList(sqlId("findByAfter"), MapBuilder.of().put("farmId", farmId)
                .put("sumAt", sumAt).put("updateAt", updatedAt).map());
    }

    public List<DoctorDimensionCriteria> findByDateType(Date sumAt, Date updateAt, Integer dateType, Integer orzType) {
        return getSqlSession().selectList(sqlId("findByDateType"),
                MapBuilder.of().put("sumAt", sumAt).put("updateAt", updateAt)
                        .put("dateType", dateType).put("orzType", orzType).map());
    }

    public Integer farmStart(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("farmStart"), dimensionCriteria);
    }

    public Integer farmEnd(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("farmEnd"), dimensionCriteria);
    }

    public Integer orgStart(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("orgStart"), dimensionCriteria);
    }

    public Integer orgEnd(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("orgEnd"), dimensionCriteria);
    }

    public Integer orgDayAvgLiveStock(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("orgDayAvgLiveStock"), dimensionCriteria);
    }
}
