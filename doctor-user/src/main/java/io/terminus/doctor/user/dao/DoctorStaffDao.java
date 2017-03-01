package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
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

    public DoctorStaff findByFarmIdAndUserId(Long farmId, Long userId){
        return sqlSession.selectOne(sqlId("findByFarmIdAndUserId"), ImmutableMap.of("farmId", farmId, "userId", userId));
    }

    public List<DoctorStaff> findByFarmIdAndStatus(Long farmId, Integer status){
        return sqlSession.selectList(sqlId("findByFarmIdAndStatus"), MapBuilder.newHashMap().put("farmId", farmId, "status", status).map());
    }
}
