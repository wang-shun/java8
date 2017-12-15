package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Repository
public class DoctorPigDailyDao extends MyBatisDao<DoctorPigDaily> {

    public DoctorPigDaily findBy(Long farmId, String sumAt) {
        return sqlSession.selectOne(sqlId("findBy"), ImmutableMap.of("farmId", farmId, "sumAt", sumAt));
    }

    public DoctorPigDaily findBy(Long farmId, Date sumAt) {
        return findBy(farmId, DateUtil.toDateString(sumAt));
    }

    public DoctorPigDaily countByFarm(Long farmId, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("startDate", start);
        params.put("endDate", end);

        return sqlSession.selectOne("report", params);
    }


    public List<DoctorPigDaily> findByOrg(List<Long> farmIds, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("startDate", start);
        params.put("endDate", end);

        return sqlSession.selectOne("report", params);
    }

    /**
     * 更新日期(包括更新日期)之后每日母猪存栏
     * @param farmId 猪群id
     * @param sumAt 日期
     * @param liveChangeCount 存栏变动数量
     * @param phChangeCount 配怀舍存栏变化
     * @param cfChangeCount 产房存栏变化
     *
     */
    public void updateDailySowPigLiveStock(Long farmId, Date sumAt, Integer liveChangeCount, Integer phChangeCount, Integer cfChangeCount) {
        getSqlSession().update(sqlId("updateDailySowPigLiveStock"), MapBuilder.of().put("farmId", farmId)
                .put("sumAt", DateUtil.toDateString(sumAt)).put("liveChangeCount", liveChangeCount)
                .put("phChangeCount", phChangeCount).put("cfChangeCount", cfChangeCount).map());
    }

    /**
     * 更新日期(包括更新日期)之后每日公猪存栏
     * @param farmId 猪群id
     * @param sumAt 日期
     * @param changeCount 变动数量
     */
    public void updateDailyBoarPigLiveStock(Long farmId, Date sumAt, Integer changeCount) {
        getSqlSession().update(sqlId("updateDailyBoarPigLiveStock"), ImmutableMap.of("farmId"
                , farmId, "sumAt", DateUtil.toDateString(sumAt), "changeCount", changeCount));
    }
}
