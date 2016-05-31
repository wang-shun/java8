package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorBreed;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 品种表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorBreedDao extends MyBatisDao<DoctorBreed> {

    public List<DoctorBreed> findAll() {
        return getSqlSession().selectList(sqlId("findAll"));
    }
}
