package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 18/2/10.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorUpdateFarmDao {
    @Autowired
    protected SqlSessionTemplate sqlSession;

    public Boolean updateFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateBarnFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateBarnFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updatePigFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updatePigFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateGroupFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateGroupFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateWareHouseFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateWareHouseFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateFeedFormulaFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateFeedFormulaFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updatePigDailyFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updatePigDailyFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateGroupDailyFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateGroupDailyFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportBoarFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportBoarFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportDeliverFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportDeliverFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportEfficiencyFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportEfficiencyFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportFattenFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportFattenFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportMaterialFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportMaterialFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportMatingFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportMatingFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportNurseryFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportNurseryFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportReseverFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportReseverFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    public Boolean updateReportSowFarmName(Long farmId, String name) {
        return sqlSession.update(sql("updateReportSowFarmName"), ImmutableMap.of("farmId", farmId, "name", name)) == 1;
    }

    private String sql(String sql) {
        return "UpdateFarm." + sql;
    }
}
