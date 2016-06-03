package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public List<DoctorPigTypeStatistic> findByOrgId(Long orgId) {
        return getSqlSession().selectList(sqlId("findByOrgId"), orgId);
    }

    /**
     * 根据farmId更新统计数据(只更新统计, 不更新其他信息)
     */
    public Boolean updateByFarmId(DoctorPigTypeStatistic pigTypeStatistic) {
        return getSqlSession().update("updateByFarmId", pigTypeStatistic) == 1;
    }
}
