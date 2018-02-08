package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Desc: 用户数据权限表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-18
 */
@Repository
public class DoctorUserDataPermissionDao extends MyBatisDao<DoctorUserDataPermission> {

    public DoctorUserDataPermission findByUserId(Long userId){
        return sqlSession.selectOne(sqlId("findByUserId"), userId);
    }

    public DoctorUserDataPermission findFrozenByUserId(Long userId){
        return sqlSession.selectOne(sqlId("findFrozenByUserId"), userId);
    }

    public List<DoctorUserDataPermission> findByUserIds(List<Long> userIds){
        if(userIds == null || userIds.isEmpty()){
            return Collections.emptyList();
        }
        return sqlSession.selectList(sqlId("findByUserIds"), userIds);
    }

    public List<DoctorUserDataPermission> findByOrgId(Long orgId) {
        return sqlSession.selectList(sqlId("findByOrgId"), orgId);
    }

    public List<DoctorUserDataPermission> findByFarmId(Long farmId) {
        return sqlSession.selectList(sqlId("findByFarmId"), farmId);
    }

    /**
     * 根据用户列表删除
     * @param list 用户id列表
     */
    public void deletesByUserIds(List<Long> list) {
        getSqlSession().delete(sqlId("deletesByUserIds"), list);
    }

    public List<DoctorUserDataPermission> findAll() {
        return getSqlSession().selectList(sqlId("findAll"));
    }

    /**
     * 查询所有拥有猪场权限的账户的权限
     * @param farmId 猪场id
     * @param userIds 账户id
     * @return 权限列表
     */
    public List<DoctorUserDataPermission> findByFarmAndPrimary(Long farmId, List<Long> userIds) {
        return getSqlSession().selectList(sqlId("findByFarmAndPrimary"), ImmutableMap.of("farmId", farmId, "userIds", userIds));
    }

    public Boolean freezeByUser(Long userId) {
        return getSqlSession().update(sqlId("freezeByUser"), userId) == 1;
    }

}
