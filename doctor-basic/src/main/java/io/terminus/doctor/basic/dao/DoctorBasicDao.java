package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 基础数据表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-24
 */
@Repository
public class DoctorBasicDao extends MyBatisDao<DoctorBasic> {

    public List<DoctorBasic> findByType(Integer type) {
        return getSqlSession().selectList(sqlId("findByType"), type);
    }

    public List<DoctorBasic> findValidByType(Integer type) {
        return getSqlSession().selectList(sqlId("findValidByType"), type);
    }

}
