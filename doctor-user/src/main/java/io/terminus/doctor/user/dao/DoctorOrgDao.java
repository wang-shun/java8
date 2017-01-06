package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorOrg;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 公司表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorOrgDao extends MyBatisDao<DoctorOrg> {

    public DoctorOrg findByName(String orgName){
        return sqlSession.selectOne(sqlId("findByName"), orgName);
    }

    public List<DoctorOrg> findAll(){
        return sqlSession.selectList(sqlId("findAll"));
    }
}
