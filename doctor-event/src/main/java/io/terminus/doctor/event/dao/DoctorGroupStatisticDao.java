package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorGroupStatisticDao {
    private final SqlSessionTemplate sqlSession;

    @Autowired
    public DoctorGroupStatisticDao(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    private static String sqlId(String id) {
        return "DoctorGroupStatistic." + id;
    }

    public Integer realTimeLiveStockGroup(Long farmId, Integer pigType, String date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockGroup"),
                ImmutableMap.of("farmId", farmId, "pigType", pigType, "date", date));
    }

    public Integer turnInto(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("turnInto"), criteria.toMap());
    }

    public Integer chgFarmIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("chgFarmIn"), criteria.toMap());
    }

    public Double turnIntoWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("turnIntoWeight"), criteria.toMap());
    }

    public Integer chgFarm(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("chgFarm"), criteria.toMap());
    }

    public Double chgFarmWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("chgFarmWeight"), criteria.toMap());
    }

    public Integer sale(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sale"), criteria.toMap());
    }

    public Double saleWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("saleWeight"), criteria.toMap());
    }

    public Integer dead(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("dead"), criteria.toMap());
    }

    public Integer weedOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weedOut"), criteria.toMap());
    }

    public Integer otherChange(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("otherChange"), criteria.toMap());
    }

    public Integer toNursery(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("toNursery"), criteria.toMap());
    }

    public Integer toFatten(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("toFatten"), criteria.toMap());
    }

    public Double toFattenWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("toFattenWeight"), criteria.toMap());
    }

    public Integer toHoubei(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("toHoubei"), criteria.toMap());
    }

    public Double toHoubeiWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("toHoubeiWeight"), criteria.toMap());
    }

    public Integer turnSeed(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("turnSeed"), criteria.toMap());
    }

    public Double turnOutWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("turnOutWeight"), criteria.toMap());
    }
}
