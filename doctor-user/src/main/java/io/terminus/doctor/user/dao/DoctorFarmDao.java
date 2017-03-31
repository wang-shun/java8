package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.user.model.DoctorFarm;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪场表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorFarmDao extends MyBatisDao<DoctorFarm> {

    public List<DoctorFarm> findByOrgId(Long orgId){
        return sqlSession.selectList(sqlId("findByOrgId"), orgId);
    }

    public List<DoctorFarm> findAll() {
        return sqlSession.selectList(sqlId("findAll"));
    }

    public DoctorFarm findByOutId(String outId){
        return sqlSession.selectOne(sqlId("findByOutId"), outId);
    }


    public List<DoctorFarm> findBySource(Integer source){
        return sqlSession.selectList(sqlId("findByParams"), MapBuilder.newHashMap().put("source", source).map());
    }
}
