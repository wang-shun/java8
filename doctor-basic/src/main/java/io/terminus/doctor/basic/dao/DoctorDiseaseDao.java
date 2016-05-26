package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorDisease;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 变动类型表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorDiseaseDao extends MyBatisDao<DoctorDisease> {

    public List<DoctorDisease> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
}
