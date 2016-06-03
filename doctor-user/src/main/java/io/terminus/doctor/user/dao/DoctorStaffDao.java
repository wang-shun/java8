package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorStaff;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪场职员表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-18
 */
@Repository
public class DoctorStaffDao extends MyBatisDao<DoctorStaff> {

    public DoctorStaff findByUserId(Long userId){
        return sqlSession.selectOne(sqlId("findByUserId"), userId);
    }

    public List<DoctorStaff> findByOrgId(Long orgId){
        return sqlSession.selectList(sqlId("findByOrgId"), orgId);
    }
}
