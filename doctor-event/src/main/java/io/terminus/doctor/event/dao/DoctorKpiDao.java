package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.event.handler.sow.DoctorSowMatingHandler;
import org.joda.time.DateTime;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Objects;

/**
 * Desc: 猪场月报表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Repository
public class DoctorKpiDao {

    private final SqlSessionTemplate sqlSession;

    @Autowired
    public DoctorKpiDao(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    private static String sqlId(String id) {
        return "DoctorKpi." + id;
    }

    /**
     * 预产胎数
     */
    public int getPreDelivery(Long farmId, Date startAt, Date endAt){
        if(!Objects.isNull(startAt)){
            startAt = new DateTime(startAt).minusDays(DoctorSowMatingHandler.MATING_PREG_DAYS).toDate();
        }
        if(!Objects.isNull(endAt)){
            endAt = new DateTime(endAt).minusDays(DoctorSowMatingHandler.MATING_PREG_DAYS).toDate();
        }
        return this.sqlSession.selectOne(sqlId("preDeliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt",  startAt, "endAt", endAt));
    }

    /**
     * 分娩窝数
     */
    public int getDelivery(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产活仔数
     */
    public int getDeliveryLive(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryLiveCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产健仔数
     */
    public int getDeliveryHealth(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryHealthCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产弱仔数
     */
    public int getDeliveryWeak(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryWeakCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产死仔数
     */
    public int getDeliveryDead(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryDeadCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产木乃伊数
     */
    public int getDeliveryMny(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryMnyCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 总产仔数
     */
    public int getDeliveryAll(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryAllCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均健仔数
     */
    public double getDeliveryHealthAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryHealthCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均活仔数
     */
    public double getDeliveryLiveAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryLiveCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均产仔数
     */
    public double getDeliveryAllAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("deliveryAllCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶母猪数
     */
    public int getWeanSow(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("weanSowCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪数
     */
    public int getWeanPiglet(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("weanPigletCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪均重
     */
    public double getWeanPigletWeightAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("weanPigletWeightAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均断奶数
     */
    public double getWeanPigletCountsAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(sqlId("weanPigletCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

}
