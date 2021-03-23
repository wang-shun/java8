package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.enums.PigEvent;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorPigStatisticDao {
    private final SqlSessionTemplate sqlSession;

    @Autowired
    public DoctorPigStatisticDao(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    private static String sqlId(String id) {
        return "DoctorPigStatistic." + id;
    }

    public Integer cfLiveStock(Long orgId,Long farmId, String sumAt) {
        return sqlSession.selectOne(sqlId("cfLiveStock"), ImmutableMap.of("orgId",orgId,"farmId", farmId, "sumAt", sumAt));
    }

    public Integer phLiveStock(Long orgId,Long farmId, String sumAt) {
        return sqlSession.selectOne(sqlId("phLiveStock"), ImmutableMap.of("orgId",orgId,"farmId", farmId, "sumAt", sumAt));
    }

    public Integer sowPhReserveIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhReserveIn"), criteria.toMap());
    }

    public Integer sowPhWeanIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhWeanIn"), criteria.toMap());
    }

    public Integer sowPhEntryIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhEntryIn"), criteria.toMap());
    }

    public Integer sowPhChgFarmIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhChgFarmIn"), criteria.toMap());
    }

    public Integer sowPhDead(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhDead"), criteria.toMap());
    }

    public Integer sowPhWeedOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhWeedOut"), criteria.toMap());
    }

    public Integer sowPhSale(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhSale"), criteria.toMap());
    }

    public Integer sowPhChgFarm(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhChgFarm"), criteria.toMap());
    }

    public Integer sowPhOtherOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhOtherOut"), criteria.toMap());
    }

    public Integer mateHb(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("mateHb"), criteria.toMap());
    }

    public Integer mateDn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("mateDn"), criteria.toMap());
    }

    public Integer mateFq(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("mateFq"), criteria.toMap());
    }

    public Integer mateLc(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("mateLc"), criteria.toMap());
    }

    public Integer mateYx(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("mateYx"), criteria.toMap());
    }

    public Integer matingCount(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("matingCount"), criteria.toMap());
    }

    public Integer sowPhMating(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhMating"), criteria.toMap());
    }

    public Integer sowPhKonghuai(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhKonghuai"), criteria.toMap());
    }

    public Integer sowPhPregnant(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowPhPregnant"), criteria.toMap());
    }

    public Integer pregPositive(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("pregPositive"), criteria.toMap());
    }

    public Integer pregNegative(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("pregNegative"), criteria.toMap());
    }

    public Integer pregFanqing(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("pregFanqing"), criteria.toMap());
    }

    public Integer pregLiuchan(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("pregLiuchan"), criteria.toMap());
    }

    public Integer weanMate(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanMate"), criteria.toMap());
    }

    public Integer weanDeadWeedOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanDeadWeedOut"), criteria.toMap());
    }

    public Integer sowCfIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfIn"), criteria.toMap());
    }

    public Integer sowCfInFarmIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfInFarmIn"), criteria.toMap());
    }

    public Integer sowCfDead(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfDead"), criteria.toMap());
    }

    public Integer sowCfWeedOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfWeedOut"), criteria.toMap());
    }

    public Integer sowCfSale(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfSale"), criteria.toMap());
    }

    public Integer sowCfChgFarm(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfChgFarm"), criteria.toMap());
    }

    public Integer sowCfOtherOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("sowCfOtherOut"), criteria.toMap());
    }

    public Integer earlyMating(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("earlyMating"), criteria.toMap());
    }

    public Integer earlyFarrowNest(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("earlyFarrowNest"), criteria.toMap());
    }

    public Integer laterNest(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("laterNest"), criteria.toMap());
    }

    public Integer farrowNest(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowNest"), criteria.toMap());
    }

    public Integer farrowLive(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowLive"), criteria.toMap());
    }

    public Integer farrowHealth(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowHealth"), criteria.toMap());
    }

    public Integer farrowWeak(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowWeak"), criteria.toMap());
    }

    public Integer farrowDead(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowDead"), criteria.toMap());
    }

    public Integer farrowjmh(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowjmh"), criteria.toMap());
    }

    public Double farrowWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("farrowWeight"), criteria.toMap());
    }

    public Integer weanNest(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanNest"), criteria.toMap());
    }

    public Integer weanQualifiedCount(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanQualifiedCount"), criteria.toMap());
    }

    public Integer weanCount(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanCount"), criteria.toMap());
    }

    public Integer weanDayAge(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanDayAge"), criteria.toMap());
    }

    public Double weanWeight(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("weanWeight"), criteria.toMap());
    }

    public Integer boarLiveStock(Long farmId, String sumAt) {
        return sqlSession.selectOne(sqlId("boarLiveStock"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public Integer boarIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarIn"), criteria.toMap());
    }

    public Integer boarChgFarmIn(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarChgFarmIn"), criteria.toMap());
    }

    public Integer boarDead(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarDead"), criteria.toMap());
    }

    public Integer boarWeedOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarWeedOut"), criteria.toMap());
    }

    public Integer boarSale(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarSale"), criteria.toMap());
    }

    public Integer boarOtherOut(DoctorStatisticCriteria criteria) {
        return sqlSession.selectOne(sqlId("boarOtherOut"), criteria.toMap());
    }

    public Integer sowEntryAndNotMatingNum(DoctorStatisticCriteria criteria) {

        Map<String, Object> params = criteria.toMap();
        params.put("type", PigEvent.ENTRY.getKey());

        //某一日进场母猪数
        Integer entryNum = sqlSession.selectOne(sqlId("countSow"), params);

        params.put("type", PigEvent.MATING.getKey());
        //某一日配种母猪数
        Integer matingNum = sqlSession.selectOne(sqlId("countSow"), params);

        Integer countSow = sqlSession.selectOne(sqlId("countSow1"), params);

        return countSow;

//        return entryNum - matingNum;
    }

    public List<DoctorFarmEarlyEventAtDto> earlyMateDate(String startAt){
        return sqlSession.selectList(sqlId("earlyMateDate"), startAt);
    }

    public Integer mateLeadToFarrow(Long farmId, String startAt, String endAt) {
        return sqlSession.selectOne(sqlId("mateLeadToFarrow"),
                ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }
}
