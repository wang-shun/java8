package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorOrg;
import org.apache.commons.lang3.StringUtils;
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

    public DoctorOrg findByName(String orgName) {
        return sqlSession.selectOne(sqlId("findByName"), orgName);
    }

    public List<DoctorOrg> findAll() {
        return sqlSession.selectList(sqlId("findAll"));
    }

    /**
     * 获取父公司下的子公司(直接关联)
     *
     * @param parentId 父公司id
     * @return 子公司列表
     */
    public List<DoctorOrg> findOrgByParentId(Long parentId) {
        return sqlSession.selectList(sqlId("findOrgByParentId"), parentId);
    }

    /**
     * 根据公司名字模糊搜索公司
     *
     * @param fuzzyName 模糊搜索
     * @param type      公司类型
     * @return 公司列表
     */
    public List<DoctorOrg> findByFuzzyName(String fuzzyName, Integer type) {
        return sqlSession.selectList("findByFuzzyName", ImmutableMap.of("fuzzyName", fuzzyName, "type", type));
    }

    /**
     * 绑定部门关系
     *
     * @param orgIds   子部门id
     * @param parentId 父id
     * @return 是否成功
     */
    public Boolean bindDepartment(List<Long> orgIds, Long parentId) {
        return sqlSession.update(sqlId("bindDepartment"), ImmutableMap.of("orgIds", orgIds, "parentId", parentId)) == 1;
    }

    /**
     * 解绑部门关系
     *
     * @param orgIds 子部门id
     * @return 是否成功
     */
    public Boolean unbindDepartment(List<Long> orgIds) {
        return sqlSession.update(sqlId("unbindDepartment"), ImmutableMap.of("orgIds", orgIds)) == 1;
    }

    /**
     * 查询排除这些id的公司
     *
     * @param orgIds 排除的id
     * @return 公司类别
     */
    public List<DoctorOrg> findExcludeIds(List<Long> orgIds) {
        return sqlSession.selectList(sqlId("findExcludeIds"), ImmutableMap.of("orgIds", orgIds));
    }

    /**
     * 获取某种类型的公司
     *
     * @param type 公司类型
     * @return
     */
    public List<DoctorOrg> findByType(Integer type) {
        return getSqlSession().selectList(sqlId("findByType"), type);
    }

    /**
     * 根据手机号查找公司
     *
     * @param mobile
     * @return DoctorOrg
     */
    public DoctorOrg findByMobile(String mobile) {

        if (StringUtils.isBlank(mobile))
            return null;
        return sqlSession.selectOne(sqlId("findByMobile"), mobile);
    }

    /**
     * 修改公司的名称 doctor_orgs表  (公司表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateName(Long id,String name){
        return sqlSession.update(sqlId("updateName"),ImmutableMap.of("id",id,"name",name))==1;
    }

    /**
     * 修改公司的名称 doctor_barns表  (猪舍表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateBarnName(Long id,String name){
        return sqlSession.update(sqlId("updateBarnName"),ImmutableMap.of("id",id,"name",name))==1;
    }


    /**
     * 修改公司的名称 doctor_farms表  (猪场表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateFarmsName(Long id,String name){
        return sqlSession.update(sqlId("updateFarmsName"),ImmutableMap.of("id",id,"name",name))==1;
    }

    /**
     * 修改公司的名称 doctor_group_events表  (猪群事件表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateGroupEventName(Long id,String name){
        return sqlSession.update(sqlId("updateGroupEventName"),ImmutableMap.of("id",id,"name",name))==1;
    }


    /**
     * 修改公司的名称 doctor_groups表  (猪群卡片表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateGroupName(Long id,String name){
        return sqlSession.update(sqlId("updateGroupName"),ImmutableMap.of("id",id,"name",name))==1;
    }



    /**
     * 修改公司的名称 doctor_pig_events表  (猪只事件表)
     * @param id
     * @param name
     * @return
     */
    public boolean updatePigEventsName(Long id,String name){
        return sqlSession.update(sqlId("updatePigEventsName"),ImmutableMap.of("id",id,"name",name))==1;
    }



    /**
     * 修改公司的名称 doctor_pig_score_applys表  (猪场评分功能申请表)
     * @param id
     * @param name
     * @return
     */
    public boolean updatePigScoreApplyName(Long id,String name){
        return sqlSession.update(sqlId("updatePigScoreApplyName"),ImmutableMap.of("id",id,"name",name))==1;
    }

    /**
     * 修改公司的名称 doctor_pigs表  (猪基础信息表)
     * @param id
     * @param name
     * @return
     */
    public boolean updatePigName(Long id,String name){
        return sqlSession.update(sqlId("updatePigName"),ImmutableMap.of("id",id,"name",name))==1;
    }



    /**
     * 修改公司的名称 doctor_group_dailies表  (猪群相关报表表)
     * @param id
     * @param name
     * @return
     */
    public boolean updateGroupDaileName(Long id,String name){
        return sqlSession.update(sqlId("updateGroupDaileName"),ImmutableMap.of("id",id,"name",name))==1;
    }

    /**
     * 修改公司的名称 doctor_pig_dailies表  (猪相关报表)
     * @param id
     * @param name
     * @return
     */
    public boolean updatePigDailieName(Long id,String name){
        return sqlSession.update(sqlId("updatePigDailieName"),ImmutableMap.of("id",id,"name",name))==1;
    }

    public DoctorOrg findName(Long id) {
        return sqlSession.selectOne(sqlId("findName"), id);
    }

    /**
     * （jiangsj）用户审核通过后把公司的parent_id置为0、type置为2
     * @param id
     * @return
     */
    public boolean updateOrgPidTpye(Long id){
        return sqlSession.update(sqlId("updateOrgPidTpye"),ImmutableMap.of("id",id))==1;
    }

}
