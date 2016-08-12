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
     * @param farmId  猪场ID
     * @param startAt 开始时间
     * @param endAt   结束时间
     * @return
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
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDelivery(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产活仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryLive(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryLiveCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产健仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryHealth(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryHealthCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产弱仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryWeak(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryWeakCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产死仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryDead(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryDeadCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产木乃伊数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryMny(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryMnyCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 总产仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDeliveryAll(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryAllCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均健仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public double getDeliveryHealthAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryHealthCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均活仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public double getDeliveryLiveAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryLiveCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均产仔数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public double getDeliveryAllAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryAllCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶母猪数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getWeanSow(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("weanSowCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getWeanPiglet(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("weanPigletCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪均重
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public double getWeanPigletWeightAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("weanPigletWeightAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均断奶数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public double getWeanPigletCountsAvg(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("weanPigletCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

}
