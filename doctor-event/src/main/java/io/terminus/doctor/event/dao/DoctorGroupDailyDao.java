package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
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

    public List<DoctorGroupDaily> findByAfter(Date updatedAt){
        return getSqlSession().selectList(sqlId("findByAfter"), updatedAt);
    }

    public List<DoctorDimensionCriteria> findByDateType(Date sumAt, Integer dateType, Integer orzType) {
        return getSqlSession().selectList(sqlId("findByDateType"),
                ImmutableMap.of("sumAt", sumAt, "dateType", dateType, "orzType", orzType));
    }

    public Integer start(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("start"), dimensionCriteria);
    }

    public Integer end(DoctorDimensionCriteria dimensionCriteria){
        return getSqlSession().selectOne(sqlId("end"), dimensionCriteria);
    }
}
