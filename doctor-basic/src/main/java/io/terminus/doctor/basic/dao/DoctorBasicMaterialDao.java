package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 基础物料表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Repository
public class DoctorBasicMaterialDao extends MyBatisDao<DoctorBasicMaterial> {

    public List<DoctorBasicMaterial> findAll() {
        return getSqlSession().selectList(sqlId("findAll"));
    }
}
