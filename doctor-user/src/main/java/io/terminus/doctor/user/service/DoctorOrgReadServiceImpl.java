package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.terminus.common.utils.Arguments.notEmpty;

@Slf4j
@Service
@RpcProvider
public class DoctorOrgReadServiceImpl implements DoctorOrgReadService{

    private final DoctorOrgDao doctorOrgDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorOrgReadServiceImpl(DoctorOrgDao doctorOrgDao,
                                    DoctorUserDataPermissionDao doctorUserDataPermissionDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Override
    public Response<DoctorOrg> findOrgById(Long orgId) {
        Response<DoctorOrg> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findById(orgId));
        } catch (Exception e) {
            log.error("find org by id failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }
    @Override
    public Response<List<DoctorOrg>> findOrgByIds(List<Long> orgIds) {
        Response<List<DoctorOrg>> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findByIds(orgIds));
        } catch (Exception e) {
            log.error("find org by id failed, orgIds:{}, cause:{}", orgIds, Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorOrg>>  findOrgByGroup(List<Long> orgIds,Long groupId){
        Response<List<DoctorOrg>> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findOrgByGroup(orgIds,groupId));
        } catch (Exception e) {
            log.error("find org by id failed, orgIds:{}, cause:{}", orgIds, Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorOrg>> findOrgsByUserId(Long userId) {
        try {
            DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
            if (permission == null || !notEmpty(permission.getOrgIdsList())) {
                return Response.ok(Collections.emptyList());
            }
            return Response.ok(doctorOrgDao.findByIds(permission.getOrgIdsList()));
        } catch (Exception e) {
            log.error("find orgs by userId failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("org.find.fail");
        }
    }

    @Override
    public Response<List<DoctorOrg>> findAllOrgs() {
        try {
            return Response.ok(doctorOrgDao.findAll());
        } catch (Exception e) {
            log.error("find all orgs failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("org.find.fail");
        }
    }

    @Override
    public Response<List<DoctorOrg>> findOrgByParentId(Long parentId) {
        try {
            return Response.ok(doctorOrgDao.findOrgByParentId(parentId));
        } catch (Exception e) {
            log.error("find org by parentId failed, parentId:{}, cause:{}", parentId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.org.by.parentId.failed");
        }
    }

    @Override
    public Response<List<DoctorOrg>> suggestOrg(String fuzzyName, Integer type) {
        try {
            return Response.ok(doctorOrgDao.findByFuzzyName(fuzzyName, type));
        } catch (Exception e) {
            log.error("suggest org failed, fuzzyName:{}, type:{}, cause:{}"
                    , fuzzyName, type, Throwables.getStackTraceAsString(e));
            return Response.fail("suggest.org.failed");
        }
    }

    @Override
    public Response<Paging<DoctorOrg>> paging(Map<String, Object> criteria, Integer pageSize, Integer pageNo) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(doctorOrgDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
         log.error("paging org failed, doctorOrg:{}, cause:{}", criteria, Throwables.getStackTraceAsString(e));
            return Response.fail("paging.org.failed");
        }
    }

    @Override
    public Response<DoctorOrg> findByName(String name) {
        try {
            return Response.ok(doctorOrgDao.findByName(name));
        } catch (Exception e) {
            log.error("find by name failed, name:{}, cause:{}", name, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.name.failed");
        }
    }

    @Override
    public Response<Boolean> updateOrgPidTpye(Long id) {
        return null;
    }

    /**
     * 通过公司id查集团(孔景军)
     * @param orgId
     * @return
     */
    @Override
    public Response<DoctorOrg>  findGroupcompanyByOrgId(Long orgId){
        return Response.ok(doctorOrgDao.findGroupcompanyByOrgId(orgId));
    }

    @Override
    public Integer  findUserTypeById(Long userId){
        return doctorOrgDao.findUserTypeById(userId);
    }
    @Override
    public List<Map<String,Object>> getOrgcunlan(Long groupId,List<Long> orgIds){
        List<Map<String,Object>> result = new ArrayList<>();
        List<Map<String,Object>> orgList = doctorOrgDao.getOrgByGroupId(groupId,orgIds);
        orgList.parallelStream().forEach(map -> {
            List<Map<Object,String>> orgCunlan = doctorOrgDao.getCunlan((long)map.get("id"));
            Map<String,Object> maps = new HashMap<>();
            maps.put("orgName",map.get("name"));
            maps.put("orgCunlan",orgCunlan);
            result.add(maps);
        });
        return result;
    }
    @Override
    public Map<Object,String> getGroupcunlan(Long groupId){
        String groupName = doctorOrgDao.getGroupNameById(groupId);
        List<Long> orgList = doctorOrgDao.getOrgByGroupId1(groupId);
        List<Map<Object,String>> a = new ArrayList<>();
        if(orgList.size() == 0){
            a= null;
        }else {
            a = doctorOrgDao.getGroupCunlan(orgList);
        }
        Map map = new HashMap();
        map.put("groupName",groupName);
        map.put("data",a);
        return map;
    }
    /**
     * 员工查询1
     */
    @Override
    public Response staffQuery(Map<String, Object> params) {
        Paging<Map<String,Object>> paging = doctorOrgDao.staffQuery(params);
        return Response.ok(paging);
    }

}
