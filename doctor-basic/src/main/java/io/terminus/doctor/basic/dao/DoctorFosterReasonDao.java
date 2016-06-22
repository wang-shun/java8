package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorFosterReason;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 寄养原因表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-22
 */
@Repository
public class DoctorFosterReasonDao extends MyBatisDao<DoctorFosterReason> {

    public List<DoctorFosterReason> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
}
