package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import org.springframework.stereotype.Repository;

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
}
