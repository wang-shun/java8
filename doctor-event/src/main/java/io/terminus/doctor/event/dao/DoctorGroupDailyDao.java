package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import org.springframework.stereotype.Repository;

import java.util.Date;

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
}
