package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorChgFarmInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xjn on 18/4/20.
 * email:xiaojiannan@terminus.io
 */
@Repository
public class DoctorChgFarmInfoDao extends MyBatisDao<DoctorChgFarmInfo>{
    public DoctorChgFarmInfo findByFarmIdAndPigId(Long farmId, Long pigId) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndPigId"), ImmutableMap.of("farmId", farmId, "pigId", pigId));
    }

    public List<DoctorChgFarmInfo> findByPigId(Long pigId) {
        return getSqlSession().selectList(sqlId("findByPigId"), pigId);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/feature/warehouse-v2
