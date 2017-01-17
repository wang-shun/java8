package io.terminus.doctor.basic.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import org.springframework.stereotype.Repository;

/**
 * Desc: 猪场基础数据关联表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-11-21
 */
@Repository
public class DoctorFarmBasicDao extends MyBatisDao<DoctorFarmBasic> {

    public DoctorFarmBasic findByFarmId(Long farmId) {
        return getSqlSession().selectOne("findFarmBasicsByFarmId", ImmutableMap.of("farmId", farmId));
    }
}
