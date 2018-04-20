package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorChgFarmRecord;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 18/4/20.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorChgFarmRecordDao extends MyBatisDao<DoctorChgFarmRecord>{
    public DoctorChgFarmRecord findByFarmIdAndPigId(Long farmId, Long pigId) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndPigId"), ImmutableMap.of("farmId", farmId, "pigId", pigId));
    }
}
