package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorOrg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc: 公司表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorOrgDao extends MyBatisDao<DoctorOrg> {

    // 查询公司时得到有集团的公司 （陈娟 2018-08-31）
    public List<DoctorOrg> getParentId(Map<String, Object> criteria) {
        return this.sqlSession.selectList(this.sqlId("getParentId"), criteria);
    }

    public List<DoctorOrg> findOrgByParentIdAndName(Long parentId,String fuzzyName) {
        return sqlSession.selectList("findOrgByParentIdAndName", ImmutableMap.of("parentId", parentId, "fuzzyName", fuzzyName));
    }

    // 集团，公司的数据展示（陈娟 2018-8-29）
    public Paging<DoctorOrg> pagingCompany(Integer offset, Integer limit, Map<String, Object> criteria) {
        if (criteria == null) {
            criteria = Maps.newHashMap();
        }

        Long total = (Long)this.sqlSession.selectOne(this.sqlId("countCompany"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map)criteria).put("offset", offset);
            ((Map)criteria).put("limit", limit);
            List<DoctorOrg> datas = this.sqlSession.selectList(this.sqlId("pagingCompany"), criteria);
            return new Paging(total, datas);
        }
    }

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

    //关联子公司得到公司（陈娟 2018-08-29）
    public List<DoctorOrg> getCompanyByName(String name) {
        return getSqlSession().selectList(sqlId("getCompanyByName"), ImmutableMap.of("name", name));
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
    public boolean updateName(Long id,String name,Integer type){
        return sqlSession.update(sqlId("updateName"),ImmutableMap.of("id",id,"name",name,"type",type))==1;
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
     * 通过公司id查集团(孔景军)
     * @param orgId
     * @return
     */
    public DoctorOrg  findGroupcompanyByOrgId(Long orgId){
        return sqlSession.selectOne(sqlId("findGroupcompanyByOrgId"), orgId);
    }
    /*
    孔景军
     */
    public List<DoctorOrg>  findOrgByGroup(List<Long> orgIds,Long groupId){
        Map map = new HashMap();
        map.put("orgIds",orgIds);
        map.put("groupId",groupId);
        return sqlSession.selectList(sqlId("findOrgByGroup"), map);
    }
    public Integer findUserTypeById(Long userId){
        return sqlSession.selectOne(sqlId("findUserTypeById"), userId);
    }

    public List<Map<String,Object>> getOrgByGroupId(Long groupId){
        return sqlSession.selectList(sqlId("getOrgByGroupId"), groupId);
    }
    public List<Long> getOrgByGroupId1(Long groupId){
        return sqlSession.selectList(sqlId("getOrgByGroupId1"), groupId);
    }
    public List<Map<Object,String>> getCunlan(Long orgId){
        return sqlSession.selectList(sqlId("getCunlan"), orgId);
    }
    public List<Map<Object,String>> getGroupCunlan(List<Long> orgId){
        return sqlSession.selectList(sqlId("getGroupCunlan"), orgId);
    }
    public String getGroupNameById(Long orgId) {
        return sqlSession.selectOne(sqlId("getGroupNameById"), orgId);
    }
    /**
     * 员工查询1
     */
    public Paging<Map<String,Object>> staffQuery(Map<String, String> params){
        Long total = this.sqlSession.selectOne(sqlId("staffCount"), params);
        if (total == 0){
            return new Paging(0L, Collections.emptyList());
        } else {
            int pageNo = Integer.parseInt(params.get("pageNo")) * Integer.parseInt(params.get("pageSize"))
                    - Integer.parseInt(params.get("pageSize"));
            params.put("pageNo", pageNo + "");
            List<Map<String,Object>> datas = this.sqlSession.selectList(this.sqlId("staffQuery"), params);
//            for (Iterator<Map<String,Object>> it = datas.iterator(); it.hasNext();){
//
//            }
            return new Paging(total, datas);
        }
    }
}
