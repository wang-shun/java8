package io.terminus.doctor.user.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.user.model.DoctorFarm;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc: 猪场表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorFarmDao extends MyBatisDao<DoctorFarm> {

    // 判断猪场编码是否已存在 （陈娟 2018-10-09）
    public DoctorFarm findByCode(String farmCode) {
        return sqlSession.selectOne(sqlId("findByCode"), farmCode);
    }

    public List<DoctorFarm> findByOrgId(Long orgId){
        return sqlSession.selectList(sqlId("findByOrgId"), orgId);
    }

    public List<DoctorFarm> findByOrgIds(List<Long> orgIds){
        return sqlSession.selectList(sqlId("findByOrgIds"), orgIds);
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

    public List<DoctorFarm> findFarmsByIds(List<Long> list) {
        return getSqlSession().selectList(sqlId("findFarmsByIds"), list);
    }

    public List<DoctorFarm> findFarmsBy(Long orgId, Integer isIntelligent) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("orgId", orgId);
        map.put("isIntelligent", isIntelligent);
        return getSqlSession().selectList(sqlId("findFarmsBy"), map);
    }

    public Boolean freeze(Long id) {
        return getSqlSession().update(sqlId("freeze"), id) == 1;
    }


    public DoctorFarm findByNumber(String number) {
        return getSqlSession().selectOne(sqlId("findByNumber"), number);
    }

    public List<DoctorFarm> findByName(String name) {
        return getSqlSession().selectList(sqlId("findByName"), name);
    }
}
