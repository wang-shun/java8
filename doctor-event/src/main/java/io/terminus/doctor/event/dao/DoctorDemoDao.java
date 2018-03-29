package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorDemo;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
@Repository
public class DoctorDemoDao extends MyBatisDao<DoctorDemo> {

    public DoctorDemo findByName(String name) {
        return getSqlSession().selectOne(sqlId("findByName"), name);
    }
}
