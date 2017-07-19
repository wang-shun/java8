package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
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

    /**
     * 获取父公司下的子公司(直接关联)
     * @param parentId 父公司id
     * @return 子公司列表
     */
    public List<DoctorOrg> findOrgByParentId(Long parentId) {
        return sqlSession.selectList(sqlId("findOrgByParentId"), parentId);
    }

    /**
     * 根据公司名字模糊搜索公司
     * @param fuzzyName
     * @param type 公司类型
     * @return
     */
    public List<DoctorOrg> findByFuzzyName(String fuzzyName, Integer type) {
        return sqlSession.selectList("findByFuzzyName", ImmutableMap.of("fuzzyName", fuzzyName, "type", type));
    }
}
