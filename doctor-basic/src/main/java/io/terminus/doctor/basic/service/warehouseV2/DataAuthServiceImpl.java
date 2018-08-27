package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DataAuthDao;
import io.terminus.doctor.basic.dto.warehouseV2.DataAuth;
import io.terminus.doctor.basic.dto.warehouseV2.DataSubRole;
import io.terminus.doctor.common.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DataAuthServiceImpl
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:08
 */
@Slf4j
@Service
@RpcProvider
public class DataAuthServiceImpl implements DataAuthService{

    @Autowired
    private DataAuthDao dataAuthDao;

    /**
     * 查询用户角色数据
     * @return
     */
    @Override
    public Response getUserRoleInfo(Map<String, String> params) {
        try{
            String pageNo = params.get("pageNo");
            String pageSize = params.get("pageSize");
            if(StringUtils.isBlank(pageNo)) return Response.fail("pageNo必传");
            if(StringUtils.isBlank(pageSize)) return Response.fail("pageSize必传");

            Map<String,Object> resultMap = Maps.newHashMap();
            resultMap.put("total",dataAuthDao.getUserRoleInfoCount(params));
            resultMap.put("datas",dataAuthDao.getUserRoleInfo(params));
            return Response.ok(resultMap);
        }catch (Exception e){
            log.error("getUserRoleInfo[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

    @Override
    public Response userSingleRoleInfo(Integer userId) {
        try{
            return Response.ok(dataAuthDao.userSingleRoleInfo(userId));
        }catch (Exception e){
            log.error("userSingleRoleInfo[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 添加或修改用户角色数据
     * @return
     */
    @Override
    @Transactional
    public Response editUserRoleInfo(Map<String, String> params) {
        try{
            String editType = params.get("editType");
            String userName = params.get("userName");
            String mobile = params.get("mobile");

            if(StringUtils.isBlank(editType)) return Response.fail("editType必传");
            if(StringUtils.isBlank(userName)) return Response.fail("userName必传");
            if(StringUtils.isBlank(mobile)) return Response.fail("mobile必传");

            if(editType.toUpperCase().equals("A")) //新增
            {
                if(dataAuthDao.selectUserByName(userName) != null ||
                        dataAuthDao.selectUserByMobile(mobile) != null) {
                    return Response.fail("该用户对应的用户名或手机号已存在");
                }

                String password = params.get("password");
                if(StringUtils.isBlank(password)) return Response.fail("password必传");

                params.put("password", EncryptUtil.encrypt(password));
                dataAuthDao.insertUser(params);
                Integer userId = dataAuthDao.selectUserByName(userName);
                params.put("userId",userId.toString());
                dataAuthDao.insertUserRole(params);

            }
            else  //编辑
            {
                String userId = params.get("userId");
                if(StringUtils.isBlank(userId)) return Response.fail("userId必传");

                if(dataAuthDao.selectUserById(userId) == null) {
                    return Response.fail("该用户不存在,不能修改");
                }

                String password = params.get("password");
                if(StringUtils.isNotBlank(password)){
                    params.put("password", EncryptUtil.encrypt(password));
                }

                dataAuthDao.updateUser(params);
                dataAuthDao.updateUserRole(params);

            }
            return Response.ok();
        }catch (Exception e){
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("editUserRoleInfo[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询用户数据范围授权
     * @return
     */
    @Override
    public Response getDataSubRoles(Integer userId) {
        try{
            Map<String,Object> resultMap = Maps.newHashMap();

            List<Map<String,Object>> mapList = dataAuthDao.selectTreeAll();
            if(null == mapList || mapList.size() == 0){
                return Response.fail("没有集团、公司及猪场数据");
            }

            // 循环处理所有集团、公司、猪场数据
            String tempGroupId = "";
            Map<String,Object> reMap = Maps.newLinkedHashMap();
            List<Map<String,Object>> orgList = Lists.newArrayList();
            Map<String,Object> orgMap;
            for (Map<String,Object> subMap : mapList) {
                String groupId = String.valueOf(subMap.get("groupId"));
                String groupName = String.valueOf(subMap.get("groupName"));
                if(groupId != null && !groupId.equals(tempGroupId))
                {
                    orgMap = Maps.newLinkedHashMap();
                    orgMap.put("key","group_" + groupId);
                    orgMap.put("name",groupName);

                    //处理公司数据,过滤得到新的集合数组
                    List<Map<String,Object>> orgLists = getOrgLists(mapList,groupId);
                    List<Map<String,Object>> reorgLists = null;
                    if(orgLists != null && orgLists.size() > 0)
                    {
                        reorgLists = Lists.newArrayList();
                        for (Map<String,Object> subOrgMap : orgLists)
                        {
                            Map<String,Object> reSubOrgMap = subOrgMap;
                            String orgId = String.valueOf(subOrgMap.get("key"));
                            //处理猪场数据,过滤得到新的集合数组
                            List<Map<String,Object>> farmLists = getFarmLists(mapList,groupId,orgId);
                            reSubOrgMap.put("children",farmLists);
                            reorgLists.add(reSubOrgMap);
                        }
                    }
                    orgMap.put("children",reorgLists);
                    orgList.add(orgMap);
                }
                tempGroupId = groupId;
            }
            reMap.put("children",orgList);
            resultMap.put("datas",reMap);

            if(userId != null) {
                Map<String, Object> map = dataAuthDao.selectUserPermission(userId);
                String group_ids = String.valueOf(map.get("group_ids"));
                String org_ids = String.valueOf(map.get("org_ids"));
                String farm_ids = String.valueOf(map.get("farm_ids"));

                Map<String,Object> vmap = Maps.newLinkedHashMap();
                if(StringUtils.isNotBlank(group_ids)){
                    String [] groupIds = group_ids.split(",");
                    List<String> groupLists = Lists.newArrayList();
                    for(String groupId : groupIds)
                    {
                        groupLists.add("group_" + groupId);
                    }
                    vmap.put("groupLists",groupLists);
                }

                if(StringUtils.isNotBlank(org_ids)){
                    String [] orgIds = org_ids.split(",");
                    List<String> orgLists = Lists.newArrayList();
                    for(String orgId : orgIds)
                    {
                        orgLists.add("org_"+ orgId);
                    }
                    vmap.put("orgLists",orgLists);
                }

                if(StringUtils.isNotBlank(farm_ids)){
                    String [] farmIds = farm_ids.split(",");
                    List<String> farmLists = Lists.newArrayList();
                    for(String farmId : farmIds)
                    {
                        farmLists.add("farm_"+ farmId);
                    }
                    vmap.put("farmLists",farmLists);
                }

                String userType = dataAuthDao.selectUserType(userId);
                resultMap.put("userPerssion", vmap);
                resultMap.put("userType", userType);
            }

            return Response.ok(resultMap);
        }catch (Exception e){
            log.error("getDataSubRoles[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取指定集团下的公司集合数据
     * @param mapList
     * @param pgroupId
     * @return
     */
    private List<Map<String,Object>> getOrgLists(List<Map<String,Object>> mapList,String pgroupId){
        try
        {
            List<Map<String,Object>> orgLists = Lists.newArrayList();
            String tempOrgId = "";
            for (Map<String,Object> subMap : mapList) {
                String groupId = String.valueOf(subMap.get("groupId"));
                if(groupId != null && groupId.equals(pgroupId)) {
                    String orgId = String.valueOf(subMap.get("orgId"));
                    String orgName = String.valueOf(subMap.get("orgName"));
                    if(StringUtils.isNotBlank(orgId) && !tempOrgId.equals(orgId)) {
                        Map<String, Object> orgMap = Maps.newLinkedHashMap();
                        orgMap.put("key", "org_" + orgId);
                        orgMap.put("name", orgName);
                        orgLists.add(orgMap);
                    }
                    tempOrgId = orgId;
                }
            }
            return orgLists;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定集团公司下的猪场集合数据
     * @param mapList
     * @param pgroupId
     * @param porgId
     * @return
     */
    private List<Map<String,Object>> getFarmLists(List<Map<String,Object>> mapList,String pgroupId,String porgId){
        try
        {
            List<Map<String,Object>> farmLists = Lists.newArrayList();
            for (Map<String,Object> subMap : mapList) {
                String groupId = String.valueOf(subMap.get("groupId"));
                String orgId = String.valueOf(subMap.get("orgId"));
                if(groupId != null && orgId != null && groupId.equals(pgroupId) && orgId.equals(porgId)) {
                    String farmId = String.valueOf(subMap.get("farmId"));
                    String farmName = String.valueOf(subMap.get("farmName"));
                    if(StringUtils.isNotBlank(farmId)) {
                        Map<String, Object> orgMap = Maps.newLinkedHashMap();
                        orgMap.put("key","farm_" + farmId);
                        orgMap.put("name", farmName);
                        farmLists.add(orgMap);
                    }
                }
            }
            return farmLists;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 批量保存用户数据范围授权
     * @param dataSubRoles
     * @return
     */
    @Override
    @Transactional
    public Response saveDataSubRoles(DataAuth dataSubRoles) {
        try{
            if(null == dataSubRoles.getUserIds() || dataSubRoles.getUserIds().size() == 0){
                return Response.fail("请选择用户");
            }
            if(StringUtils.isBlank(dataSubRoles.getUserType())){
                return Response.fail("请选择用户类型");
            }
            if(null == dataSubRoles.getDatas() || dataSubRoles.getDatas().size() == 0){
                return Response.fail("请选择用户可访问的数据权限");
            }

            List<String> userIds = dataSubRoles.getUserIds();
            List<Map<String,String>> userIdParams = Lists.newArrayList();
            List<Map<String,String>> userParams = Lists.newArrayList();
            for (String userId : userIds) {
                Map<String,String> userIdParam = Maps.newHashMap();
                userIdParam.put("userId",userId);
                userIdParams.add(userIdParam);

                Map<String,String> params = Maps.newHashMap();
                params.put("userType",dataSubRoles.getUserType());
                params.put("userId",userId);
                userParams.add(params);
            }
            // 删除历史数据
            dataAuthDao.deletePerssion(userIdParams);
            // 修改用户角色数据
            dataAuthDao.updateSubUserType(userParams);

            // 批量新增数据权限
            List<DataSubRole> dataSubRoleList = dataSubRoles.getDatas();
            List<Map<String,String>> dataSubRoleParams = Lists.newArrayList();
            for (DataSubRole dataSubRole : dataSubRoleList) {
                Map<String,String> dataSubRoleParam = Maps.newHashMap();
                dataSubRoleParam.put("userId",dataSubRole.getUserId());
                dataSubRoleParam.put("groupIds",dataSubRole.getGroupIds());
                dataSubRoleParam.put("orgIds",dataSubRole.getOrgIds());
                dataSubRoleParam.put("farmIds",dataSubRole.getFarmIds());
                dataSubRoleParams.add(dataSubRoleParam);
            }
            dataAuthDao.insertPerssion(dataSubRoleParams);

            return Response.ok("执行成功");
        }catch (Exception e){
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("saveDataSubRoles[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

}
