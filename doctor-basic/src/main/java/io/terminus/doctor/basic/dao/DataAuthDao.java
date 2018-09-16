package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.dto.warehouseV2.DataAuth;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @ClassName DataAuthDao
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:23
 */
@Repository
public class DataAuthDao extends MyBatisDao<DataAuth> {

    public List<Map<String,Object>> selGroups() {
        return getSqlSession().selectList(sqlId("selGroups"));
    }

    public List<Map<String,Object>> selOrgs(Integer groupId) {
        return getSqlSession().selectList(sqlId("selOrgs"),groupId);
    }

    public List<Map<String,Object>> selFarms(Integer orgId) {
        return getSqlSession().selectList(sqlId("selFarms"),orgId);
    }
    /**
     * 查询用户角色数据总数
     * @return
     */
    public int getUserRoleInfoCount(Map<String, String> params) {
        return getSqlSession().selectOne(sqlId("getUserRoleInfoCount"), params);
    }

    /**
     * 查询用户角色数据
     * @return
     */
    public List<Map<String,Object>> getUserRoleInfo(Map<String, String> params) {
        Integer pageNo =  Integer.parseInt(params.get("pageNo"));
        Integer pageSize =  Integer.parseInt(params.get("pageSize"));
        pageNo = (pageNo - 1) * pageSize;
        params.put("pageNo",pageNo.toString());
        return getSqlSession().selectList(sqlId("getUserRoleInfo"), params);
    }

    /**
     * 查询单个用户角色数据
     * @return
     */
    public Map<String,Object> userSingleRoleInfo(Integer userId) {
        return getSqlSession().selectOne(sqlId("userSingleRoleInfo"), userId);
    }

    /**
     * 通过用户名称查询用户id
     * @return
     */
    public Integer selectUserByName(String userName) {
        return getSqlSession().selectOne(sqlId("selectUserByName"), userName);
    }

    /**
     * 通过用户id查询用户id
     * @return
     */
    public Integer selectUserById(String userId) {
        return getSqlSession().selectOne(sqlId("selectUserById"), userId);
    }

    /**
     * 通过用户手机号查询用户id
     * @return
     */
    public Integer selectUserByMobile(String mobile) {
        return getSqlSession().selectOne(sqlId("selectUserByMobile"), mobile);
    }

    /**
     * 添加用户
     * @return
     */
    public int insertUser(Map<String, String> params) {
        return getSqlSession().insert(sqlId("insertUser"), params);
    }

    /**
     * 添加用户角色
     * @return
     */
    public int insertUserRole(Map<String, String> params) {
        return getSqlSession().insert(sqlId("insertUserRole"), params);
    }

    public int insertUserStaff(Map<String, String> params) {
        return getSqlSession().insert(sqlId("insertUserStaff"), params);
    }

    /**
     * 修改用户
     * @return
     */
    public int updateUser(Map<String, String> params) {
        return getSqlSession().insert(sqlId("updateUser"), params);
    }

    /**
     * 修改用户角色
     * @return
     */
    public int updateUserRole(Map<String, String> params) {
        return getSqlSession().insert(sqlId("updateUserRole"), params);
    }

    public int updateUserStaff(Map<String, String> params) {
        return getSqlSession().insert(sqlId("updateUserStaff"), params);
    }

    /**
     * 查询所有集团数据
     * @return
     */
    public List<Map<String,Object>> selectAllGroups(){
        return getSqlSession().selectList(sqlId("selectAllGroups"));
    }

    /**
     * 查询集团下公司数据
     * @return
     */
    public List<Map<String,Object>> selectOrgs(Map<String,String> params){
        return getSqlSession().selectList(sqlId("selectOrgs"),params);
    }

    /**
     * 查询公司下猪场数据
     * @return
     */
    public List<Map<String,Object>> selectFarms(Map<String,String> params){
        return getSqlSession().selectList(sqlId("selectFarms"),params);
    }

    /**
     * 查询用户可访问的集团、公司、猪场数据
     * @return
     */
    public Map<String,Object> selectUserPermission(Integer userId){
        return getSqlSession().selectOne(sqlId("selectUserPermission"),userId);
    }

    /**
     * 查询用户的用户类型
     * @return
     */
    public String selectUserType(Integer userId){
        return getSqlSession().selectOne(sqlId("selectUserType"),userId);
    }

    /**
     * 删除用户可访问数据权限
     * @param user_id
     * @return
     */
    public int deletePerssion(Integer [] user_id){
        return getSqlSession().delete(sqlId("deletePerssion"),user_id);
    }

    /**
     * 修改用户类型
     * @param params
     * @return
     */
    public int updateSubUserType(List<Map<String,String>> params){
        return getSqlSession().update(sqlId("updateSubUserType"),params);
    }

    /**
     * 新增用户可访问的数据权限
     * @param params
     * @return
     */
    public int insertPerssion(List<Map<String,String>> params){
        return getSqlSession().insert(sqlId("insertPerssion"),params);
    }

    public List<Long> getOrgIdList(List<Long> farmIdList){
        return getSqlSession().selectList(sqlId("getOrgIdList"),farmIdList);
    }

    public List<Long> getGroupIdList(List<Long> OrgIdsList){
        return getSqlSession().selectList(sqlId("getGroupIdList"),OrgIdsList);
    }

    // user profile 的新增与编辑 （陈娟 2018-09-16）
    public int insertUserProfile(Map<String, String> params) {
        return getSqlSession().insert(sqlId("insertUserProfile"), params);
    }

    public int updateUserProfile(Map<String, String> params) {
        return getSqlSession().insert(sqlId("updateUserProfile"), params);
    }
}
