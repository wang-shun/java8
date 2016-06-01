package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorUnit;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 计量单位表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorUnitDao extends MyBatisDao<DoctorUnit> {

    public List<DoctorUnit> findAll() {
        return getSqlSession().selectList(sqlId("findAll"));
    }
}
