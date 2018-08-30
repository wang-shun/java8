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

    @Override
    public Response selGroups() {
        try{
            List<Map<String,Object>> groupIdList = dataAuthDao.selGroups();
            Map<String,Object> pp = Maps.newLinkedHashMap();
            pp.put("id","0");
            pp.put("groupName","无集团");
            groupIdList.add(pp);
            return Response.ok(groupIdList);
        }catch (Exception e){
            log.error("selGroups[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

    @Override
    public Response selOrgs(Integer groupId) {
        try{
            return Response.ok(dataAuthDao.selOrgs(groupId));
        }catch (Exception e){
            log.error("selOrgs[error] ==> {}",e);
            return Response.fail(e.getMessage());
        }
    }

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
            String groupId = params.get("groupId");
            //String orgId = params.get("orgId");
            String realName = params.get("realName");

            if(StringUtils.isBlank(editType)) return Response.fail("editType必传");
            if(StringUtils.isBlank(userName)) return Response.fail("userName必传");
            if(StringUtils.isBlank(mobile)) return Response.fail("mobile必传");
            if(StringUtils.isBlank(groupId)) return Response.fail("groupId必传");
            //if(StringUtils.isBlank(orgId)) return Response.fail("orgId必传");
            if(StringUtils.isBlank(realName)) return Response.fail("realName必传");

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
                dataAuthDao.insertUserStaff(params);
            }
            else  //编辑
            {
                String userId = params.get("userId");
                if(StringUtils.isBlank(userId)) return Response.fail("userId必传");

                if(dataAuthDao.selectUserById(userId) == null) {
                    return Response.fail("该用户不存在,不能修改");
                }

                Integer sid = dataAuthDao.selectUserByMobile(mobile);
                if(sid != null)
                {
                    if(sid == Integer.parseInt(userId)){
                        params.put("mobile",null);
                    } else {
                        return Response.fail("该用户对应的手机号已存在");
                    }
                }

                String password = params.get("password");
                if(StringUtils.isNotBlank(password)){
                    params.put("password", EncryptUtil.encrypt(password));
                }

                dataAuthDao.updateUserRole(params);
                dataAuthDao.updateUser(params);
                dataAuthDao.updateUserStaff(params);
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
            Map<String,Object> resultMap = Maps.newLinkedHashMap();

            //存放集团、公司、猪场的树状数据集合
            Map<String,Object> reMap = Maps.newLinkedHashMap();

            List<Map<String, Object>> groupZ = Lists.newArrayList();

            // 处理无集团
            Map<String, Object> sg = Maps.newLinkedHashMap();
            sg.put("key", "group_0");
            sg.put("title", "无集团");

            Map<String, Object> newOrg;
            List<Map<String, Object>> newOrgList;

            Map<String, Object> newFarm;
            List<Map<String, Object>> newFarmList;

            Map<String,String> pm = Maps.newLinkedHashMap();
            pm.put("groupId","0");
            List<Map<String, Object>> orgList =  dataAuthDao.selectOrgs(pm);
            if(orgList != null && orgList.size() > 0)
            {
                newOrgList = Lists.newArrayList();
                for (Map<String, Object> org : orgList) {
                    String orgId = String.valueOf(org.get("orgId"));
                    String orgName = String.valueOf(org.get("orgName"));

                    newOrg = Maps.newLinkedHashMap();
                    newOrg.put("key", "org_" + orgId);
                    newOrg.put("title", orgName);

                    pm = Maps.newLinkedHashMap();
                    pm.put("orgId",orgId);

                    List<Map<String, Object>> farmList = dataAuthDao.selectFarms(pm);
                    newFarmList = Lists.newArrayList();
                    if(farmList != null && farmList.size() > 0){
                        for (Map<String, Object> farm : farmList) {
                            String farmId = String.valueOf(farm.get("farmId"));
                            String farmName = String.valueOf(farm.get("farmName"));
                            newFarm = Maps.newLinkedHashMap();
                            newFarm.put("key", "farm_" + farmId);
                            newFarm.put("title", farmName);
                            newFarmList.add(newFarm);
                        }
                    }
                    newOrg.put("children",newFarmList);
                    newOrgList.add(newOrg);
                }
                sg.put("children",newOrgList);
                groupZ.add(sg);
            }

            // 处理有集团
            List<Map<String,Object>> groupList = dataAuthDao.selectAllGroups();
            if(groupList != null && groupList.size() > 0) {
                for (Map<String, Object> groupV : groupList) {
                    String groupId = String.valueOf(groupV.get("groupId"));
                    String groupName = String.valueOf(groupV.get("groupName"));

                    sg = Maps.newLinkedHashMap();
                    sg.put("key", "group_" + groupId);
                    sg.put("title", groupName);

                    pm = Maps.newLinkedHashMap();
                    pm.put("groupId",groupId);
                    orgList =  dataAuthDao.selectOrgs(pm);

                    newOrgList = Lists.newArrayList();
                    if(orgList != null && orgList.size() > 0)
                    {
                        for (Map<String, Object> org : orgList) {
                            String orgId = String.valueOf(org.get("orgId"));
                            String orgName = String.valueOf(org.get("orgName"));

                            newOrg = Maps.newLinkedHashMap();
                            newOrg.put("key", "org_" + orgId);
                            newOrg.put("title", orgName);

                            pm = Maps.newLinkedHashMap();
                            pm.put("orgId",orgId);

                            List<Map<String, Object>> farmList = dataAuthDao.selectFarms(pm);
                            newFarmList = Lists.newArrayList();
                            if(farmList != null && farmList.size() > 0){
                                for (Map<String, Object> farm : farmList) {
                                    String farmId = String.valueOf(farm.get("farmId"));
                                    String farmName = String.valueOf(farm.get("farmName"));
                                    newFarm = Maps.newLinkedHashMap();
                                    newFarm.put("key", "farm_" + farmId);
                                    newFarm.put("title", farmName);
                                    newFarmList.add(newFarm);
                                }
                            }
                            newOrg.put("children",newFarmList);
                            newOrgList.add(newOrg);
                        }
                    }
                    sg.put("children",newOrgList);
                    groupZ.add(sg);
                }
            }
            reMap.put("children", groupZ);
            resultMap.put("datas",reMap);

            if(userId != null) {
                Map<String, Object> map = dataAuthDao.selectUserPermission(userId);
                List<String> keys = Lists.newArrayList();

                if(map != null) {
                    String group_ids = String.valueOf(map.get("group_ids"));
                    String org_ids = String.valueOf(map.get("org_ids"));
                    String farm_ids = String.valueOf(map.get("farm_ids"));

                    if (StringUtils.isNotBlank(group_ids)) {
                        String[] groupIds = group_ids.split(",");
                        for (String groupId : groupIds) {
                            keys.add("group_" + groupId);
                        }
                    }

                    if (StringUtils.isNotBlank(org_ids)) {
                        String[] orgIds = org_ids.split(",");
                        for (String orgId : orgIds) {
                            keys.add("org_" + orgId);
                        }
                    }

                    if (StringUtils.isNotBlank(farm_ids)) {
                        String[] farmIds = farm_ids.split(",");
                        for (String farmId : farmIds) {
                            keys.add("farm_" + farmId);
                        }
                    }
                }

                String userType = dataAuthDao.selectUserType(userId);
                resultMap.put("userPerssion", keys);
                resultMap.put("userType", userType);
            }

            return Response.ok(resultMap);
        }catch (Exception e){
            log.error("getDataSubRoles[error] ==> {}",e);
            return Response.fail(e.getMessage());
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
            if(null == dataSubRoles.getDatas()){
                return Response.fail("请选择用户可访问的数据权限");
            }

            List<String> userIds = dataSubRoles.getUserIds();
            List<Integer> userIdParams = Lists.newArrayList();
            List<Map<String,String>> userParams = Lists.newArrayList();
            for (String userId : userIds) {
                userIdParams.add(Integer.parseInt(userId));

                Map<String,String> params = Maps.newHashMap();
                params.put("userType",dataSubRoles.getUserType());
                params.put("userId",userId);
                userParams.add(params);
            }
            // 删除历史数据
            dataAuthDao.deletePerssion(userIdParams.toArray(new Integer[0]));
            // 修改用户角色数据
            dataAuthDao.updateSubUserType(userParams);

            // 批量新增数据权限
            DataSubRole dataSubRole = dataSubRoles.getDatas();
            List<Map<String,String>> dataSubRoleParams = Lists.newArrayList();
            for (String userId : userIds) {
                Map<String,String> dataSubRoleParam = Maps.newHashMap();
                dataSubRoleParam.put("userId",userId);
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
