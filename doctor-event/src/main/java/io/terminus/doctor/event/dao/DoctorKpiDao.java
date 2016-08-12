package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.Dates;
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

    /**
     * 销售情况: 母猪
     */
    public int getSaleSow(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getSaleSow"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 销售情况: 公猪
     */
    public int getSaleBoar(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getSaleBoar"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 销售情况: 保育猪（产房+保育）
     */
    public int getSaleNursery(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getSaleNursery"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 销售情况: 育肥猪
     */
    public int getSaleFatten(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getSaleFatten"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 母猪
     */
    public int getDeadSow(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadSow"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 公猪
     */
    public int getDeadBoar(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadBoar"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 产房仔猪
     */
    public int getDeadFarrow(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadFarrow"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 保育猪
     */
    public int getDeadNursery(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadNursery"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 育肥猪
     */
    public int getDeadFatten(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadFatten"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 产房死淘率
     */
    public double getDeadFarrowRate(Long farmId, Date startAt, Date endAt) {
        Date sumAt = Dates.startOfDay(endAt);
        return sqlSession.selectOne(sqlId("getDeadFarrowRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt, "sumAt", sumAt));
    }

    /**
     * 死淘情况: 保育死淘率
     */
    public double getDeadNurseryRate(Long farmId, Date startAt, Date endAt) {
        Date sumAt = Dates.startOfDay(endAt);
        return sqlSession.selectOne(sqlId("getDeadNurseryRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt, "sumAt", sumAt));
    }

    /**
     * 死淘情况: 育肥死淘率
     */
    public double getDeadFattenRate(Long farmId, Date startAt, Date endAt) {
        Date sumAt = Dates.startOfDay(endAt);
        return sqlSession.selectOne(sqlId("getDeadFattenRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt, "sumAt", sumAt));
    }
}
