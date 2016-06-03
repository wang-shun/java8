package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import org.springframework.stereotype.Repository;

/**
 * Desc: 猪只数统计表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-03
 */
@Repository
public class DoctorPigTypeStatisticDao extends MyBatisDao<DoctorPigTypeStatistic> {

    public DoctorPigTypeStatistic findByFarmId(Long farmId) {
        return getSqlSession().selectOne(sqlId("findByFarmId"), farmId);
    }
}
