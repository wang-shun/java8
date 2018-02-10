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

    private String sql(String sql) {
        return "UpdateFarm." + sql;
    }
}
