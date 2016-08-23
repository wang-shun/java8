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
    public int getPreDelivery(Long farmId, Date startAt, Date endAt) {
        if (!Objects.isNull(startAt)) {
            startAt = new DateTime(startAt).minusDays(DoctorSowMatingHandler.MATING_PREG_DAYS).toDate();
        }
        if (!Objects.isNull(endAt)) {
            endAt = new DateTime(endAt).minusDays(DoctorSowMatingHandler.MATING_PREG_DAYS).toDate();
        }
        return this.sqlSession.selectOne(sqlId("preDeliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
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
    public int getDeliveryLive(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryLiveCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产健仔数
     */
    public int getDeliveryHealth(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryHealthCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产弱仔数
     */
    public int getDeliveryWeak(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryWeakCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产死仔数
     */
    public int getDeliveryDead(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryDeadCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 产木乃伊数
     */
    public int getDeliveryMny(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryMnyCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死黑木畸
     */
    public int getDeliveryDeadBlackMuJi(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("getDeliveryDeadBlackMuJi"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 总产仔数
     */
    public int getDeliveryAll(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryAllCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均健仔数
     */
    public double getDeliveryHealthAvg(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryHealthCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均活仔数
     */
    public double getDeliveryLiveAvg(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryLiveCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均产仔数
     */
    public double getDeliveryAllAvg(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("deliveryAllCountsAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶母猪数
     */
    public int getWeanSow(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("weanSowCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪数
     */
    public int getWeanPiglet(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("weanPigletCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 断奶仔猪均重
     */
    public double getWeanPigletWeightAvg(Long farmId, Date startAt, Date endAt) {
        return this.sqlSession.selectOne(sqlId("weanPigletWeightAvg"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 窝均断奶数
     */
    public double getWeanPigletCountsAvg(Long farmId, Date startAt, Date endAt) {
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
        return sqlSession.selectOne(sqlId("getDeadFarrowRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 保育死淘率
     */
    public double getDeadNurseryRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadNurseryRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 死淘情况: 育肥死淘率
     */
    public double getDeadFattenRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("getDeadFattenRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:配后备
     */
    public int firstMatingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("firstMatingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:配流产
     */
    public int abortionMatingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("abortionMatingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:配断奶
     */
    public int weanMatingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("weanMatingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:配阴性
     */
    public int yinMatingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("yinMatingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:配返情
     */
    public int fanQMatingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("fanQMatingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:估算受胎率
     */
    public double assessPregnancyRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("assessPregnancyRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:实际受胎率
     */
    public double realPregnancyRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("realPregnancyRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:估算分娩率
     */
    public double assessFarrowingRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("assessFarrowingRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 配种情况:实际配种分娩率
     */
    public double realFarrowingRate(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("realFarrowingRate"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 妊娠检查情况:妊娠检查阳性
     */
    public int checkYangCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("checkYangCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 妊娠检查情况:返情
     */
    public int checkFanQCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("checkFanQCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 妊娠检查情况:妊娠检查阴性
     */
    public int checkYingCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("checkYingCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 妊娠检查情况:流产
     */
    public int checkAbortionCounts(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("checkAbortionCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * NPD
     */
    public double npd(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("npd"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * psy
     */
    public double psy(Long farmId, Date startAt, Date endAt) {
        return sqlSession.selectOne(sqlId("psy"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 后备母猪(后备母猪舍)存栏
     */
    public int liveStockHoubeiSow(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockHoubeiSow"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 配怀母猪(配种舍 + 妊娠舍)存栏
     */
    public int liveStockPeihuaiSow(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockPeihuaiSow"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 产房母猪存栏(分娩舍)
     */
    public int liveStockBuruSow(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockBuruSow"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 母猪存栏
     */
    public int liveStockSow(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockSow"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 公猪存栏
     */
    public int liveStockBoar(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockBoar"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 产房仔猪存栏
     */
    public int liveStockFarrow(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockFarrow"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 保育猪存栏
     */
    public int liveStockNursery(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockNursery"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 育肥猪存栏
     */
    public int liveStockFatten(Long farmId) {
        return sqlSession.selectOne(sqlId("liveStockFatten"), ImmutableMap.of("farmId", farmId));
    }

    /**
     * 实时存栏: 获取某天的产房仔猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockFarrow(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockFarrow"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天的保育猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockNursery(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockNursery"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天的育肥猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockFatten(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockFatten"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天的后备母猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockHoubeiSow(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockHoubeiSow"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天公猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockBoar(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockBoar"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天母猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockSow(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockSow"), ImmutableMap.of("farmId", farmId, "date", date));
    }

    /**
     * 实时存栏: 获取某天在产房的母猪存栏
     * @param farmId 猪场id
     * @param date   日期
     * @return 存栏数量
     */
    public int realTimeLiveStockFarrowSow(Long farmId, Date date) {
        return sqlSession.selectOne(sqlId("realTimeLiveStockFarrowSow"), ImmutableMap.of("farmId", farmId, "date", date));
    }
}
